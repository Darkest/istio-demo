https://cookbook.openshift.org/users-and-role-based-access-control/how-can-i-enable-an-image-to-run-as-a-set-user-id.html

```shell script
oc create serviceaccount runasanyuid
oc get scc --as system:admin
oc adm policy add-scc-to-user anyuid -z runasanyuid --as system:admin
oc patch dc/minimal-notebook --patch '{"spec":{"template":{"spec":{"serviceAccountName": "runasanyuid"}}}}'
```

Allowing a user to run applications as any user ID will allow them to also run application images as root inside of the container. Because of the risks associated with allowing applications to run as root, if root isn't required, use the nonroot SCC instead of anyuid. This will allow an application to be run as any user ID except root