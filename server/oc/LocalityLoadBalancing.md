# Балансировка нагрузки по местополоению(Locality Load Balancing)

Locality определяет географиеское местоположение внутри сервис мэша с
помощью трех параметров:
- Регион
- Зона
- Под-зона

Географическое местоположение обычно представляет собой дата-центр.
Istio использвует эту информацию для распределения нагрузки по
географическому принципу Этот функционал Istio вклчен по-умолчанию. Для
выклчений данной функции необходимо задать следующий флаг при установке
istio `--set global.localityLbSetting.enabled=false`

Для определения региона\зоны\подзоны подов OpenShift использует
метки(labels) на нодах(nodes), на которых они запущены
- `failure-domain.beta.kubernetes.io/region=<регион>`
- `failure-domain.beta.kubernetes.io/zone=<зона>`
- метку для подзоны задавать не нужно так как подзна не является
  сущностью k9s (нужно уточнить что это значит)

Чтобы просмотреть все метки на нодах можно выполнить следующую команду:

`oc get nodes --show-labels`

Для из измененения меток на нодах можно использовать следующую команду:

`oc label node <нода> failure-domain.beta.kubernetes.io/zone=<зона>
--overwrite`

После смены значений меток соответствующие метки меняются и на
подах(рестарт подов не требуется) При указании в ServiceEntry блока
locality вызовы проходящие через него будут в приоритете
маршрутизироваться на ендпоинт с совпадающим с вызывающим подом
регионом\зоны\подзоны

После применения меток на нодах, запросы к сервисам внутри меша будут
маршрутизированться на ближайшие доступные поды. Когда все поды
функционируют запросы будут обрабатываться внутри одного местоположения,
в случае отказа части подов трафик перенаправляется на поды в следующее
по приоритету метоположение

Приоритет местоположений для региона\зоны us-west/zone2 выглядит так:

- Приоритет 0: us-west/zone2
- Приоритет 1: us-west/zone1, us-west/zone3
- Приоритет 2: us-east/zone1, us-east/zone2, eu-west/zone1

Иерархия приоритизации:

1. Регион
2. Зона
3. Подзона

ВАЖНО! Для того чтобы istio мог определить расположение вызывающего
клиента НЕОБХОДИМО чтобы с клиентом был связан Service Для приоритизации
трафика в соответствии по региону\зоне\подзоне НЕОБХОДИМО определить
настройку outlier(circuit-breaker) в Destination Rule. Без этого istio
не в состоянии определить статус enpoint'а и будет маршрутизировать
трафик на все enpoint'ы

Пример настройки Destination Rule:

```yaml
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: external-policy
spec:
  host: test.ext
  trafficPolicy:
    outlierDetection:
      consecutiveErrors: 1
      interval: 10s
      baseEjectionTime: 10s
      maxEjectionPercent: 100
```

## Переназначение распределние трафика в случае отказов

В некоторых случаях бывает необходимо ограничить перераспределние
трафика, чтобы исключить направление трафика в удаленные(дальние)
регионы. Это полезно в случаях если перенаправление трафика в другие
регионы не улучшит доступность сервиса или по другим причинам, например
регуляторным оограничениям.

Пример конфигурации:

```yaml
global:
  localityLbSetting:
    enabled: true
    failover:
    - from: us-east
      to: eu-west
    - from: us-west
      to: us-east
```

## Распределние нагрузки по весам

Istio также поддерживает распределение нагрузки между различными
местоположениями по заранее опреденным пропорциям.  
Например, если вы хотите оставить 80% трафика из региона us-central1
внутри самого региона, а оставшиеся 20% отправить в регион us-central2

```yaml
global:
  localityLbSetting:
    enabled: true
    distribute:
    - from: "us-central1/*"
      to:
        "us-central1/*": 80
        "us-central2/*": 20
```

# Service entry и балансировка по местоположению

Service entry позволяет добавить дополнительные записи во внутренний
регистр сервисов (service registry) istio для того, чтобы сервисы внутри
меша могли иметь доступ к этим, вручную определнным, сервисам.  
Service Entry описывает параметры сервиса, таке как DNS имя,
VIPs(вирутальные IP?), порты, протоколы, удаленные адреса(endpoints).
Сервисы, описываемые в Service entry, могут быть как внешними по
отношению к мешу(например,web APIs), так и внутренними, например, набор
виртуальных машин напрямую работающих с сервисами k8s.

Пример настройки Service entry для доступа к внешнему сервису

```yaml
apiVersion: networking.istio.io/v1alpha3
kind: ServiceEntry
metadata:
  name: external-svc-https
spec:
  hosts:
    - test.ext # Для того, чтобы сервис мог быть найден внутри кластера
              # по данному имени необходимо либо прописать его в DNS сервер, 
              # либо добавить Service
  addresses:
    - 192.168.1.13 # Адрес по которому сервисы внутри меша могут обращаться к внешнему сервису 
  location: MESH_EXTERNAL
  ports:
    - number: 8080 # Порт по которому сервисы внутри меша могут обращаться к внешнему сервису
      name: http # Имя порта, используется в дальнейшем для ассоциации порта вызова внутри меша с реальным портом вызываемой системы
      protocol: HTTP
  resolution: DNS
  endpoints: # Блок реальных адресов внешних систем
    - address: 192.168.1.13
      ports:
        http: 8081 # 'http' - имя порта из блока ports.name
      locality: 'eu-central-2/zone2' # Местоположение севиса, используется для приоритизации трафика на ближайшие сетевые интерфейсы
    - address: 192.168.1.13
      ports:
        http: 8080
      locality: 'eu-central-1/zone1'
    - address: istio-ingressgateway-istio-system.apps-crc.testing 
      # В качестве адреса также может использоваться адрес кластера openshift
      ports:
        http: 30765
      locality: 'eu-central-2/zone1'
```

В примере выше определен внешний сервис `test.ext` при обращении к
которому вызов будет перенаправлен в один из endpoint'ов, в соответствии
с настройкам балансировки нагрузки. Приведенный выше пример позволяет
настроить отказоустойчивое взаимодействие с сервисом, экземпляры
которого находятся в разных кластерах, в случае работоспособности
сервиса внутри кластера вызов будет маршрутизирован внутри кластера, в
обратном случае вызов будет перенаправлен на другой кластер в
соответствие с настройками балансировки.

Для Service Entry можно также настроить повторные запросы(идентично
другим сервисам, используя Virtual Service), по умолчанию Istio 1.4
делает 2 повтора в случае получения 503 ошибки

```yaml
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: test.ext
spec:
  hosts:
    - test.ext
  http:
    - route:
        - destination:
            host: test.ext
      retries:
        attempts: 3 
```

ВАЖНО! Circuit breaker может срабатывать не моментально и пропускать еще
какое то время запросы хотя он уже должен быть в открытом состоянии!