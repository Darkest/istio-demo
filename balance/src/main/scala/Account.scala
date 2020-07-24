import swagger.definitions.{Balance, UserAccount}
import swagger.users.{UsersHandler, UsersResource}

import scala.concurrent.Future

class Account extends UsersHandler {
  override def getAccounts(respond: UsersResource.GetAccountsResponse.type)(id: String): Future[UsersResource.GetAccountsResponse] = {
    Future.successful(respond.OK(Balance("", Vector(UserAccount("", 123.23)))))
  }
}