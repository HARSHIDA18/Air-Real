package models

import slick.jdbc.MySQLProfile.api._

class UserRepository(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def username = column[String]("username")
  def email = column[String]("email")
  def password = column[String]("password")
  def * = (id, username, email, password) <> ((User.apply _).tupled, User.unapply)
}

object UserRepository {
  val users = TableQuery[UserRepository]
}
