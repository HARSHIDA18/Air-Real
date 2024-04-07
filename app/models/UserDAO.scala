package models

import models.UserRepository.users
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.Future

class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
  private val dbConfig=dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._
  def addUser(user: User): Future[Int] = {
    db.run(users += user)
  }
  def getUserByCredentials(userName: String, email: String, password: String): Future[Option[User]] = {
    val query = users.filter(user =>
      user.username === userName && user.email === email && user.password===password
    ).result.headOption
    db.run(query)
  }
}