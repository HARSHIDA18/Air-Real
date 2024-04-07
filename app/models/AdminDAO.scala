package models

import models.AdminRepository.admins
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.Future

class AdminDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
  private val dbConfig=dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._
  def addUser(admin: Admin): Future[Int] = {
    db.run(admins += admin)
  }
  def getUserByCredentials(userName: String, email: String, password: String): Future[Option[Admin]] = {
    val query = admins.filter(admin =>
      admin.username === userName && admin.email === email && admin.password===password
    ).result.headOption
    db.run(query)
  }
}