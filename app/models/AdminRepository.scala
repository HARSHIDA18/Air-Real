package models

import slick.jdbc.MySQLProfile.api._

class AdminRepository(tag: Tag) extends Table[ Admin ](tag, "admins") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def username = column[String]("username")
  def email = column[String]("email")
  def password = column[String]("password")
  def * = (id, username, email, password) <> ((Admin.apply _).tupled, Admin.unapply)
}

object AdminRepository {
  val admins = TableQuery[AdminRepository]
}
