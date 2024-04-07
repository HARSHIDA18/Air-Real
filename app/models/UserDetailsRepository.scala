package repositories

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import models.UserBookingFormData
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

class UserDetailsRepository @Inject()(
                                       dbConfigProvider: DatabaseConfigProvider
                                     )(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class UserDetailsTable(tag: Tag) extends Table[UserBookingFormData](tag, "user_details") {
    def username = column[String]("username")
    def aircraftId = column[String]("aircraft_id")
    def aadharcardNumber = column[String]("aadharcard_number", O.PrimaryKey)
    def phoneNumber = column[String]("phone_number")
    def email = column[String]("email")

    def * = (username, aircraftId, aadharcardNumber, phoneNumber, email) <>
      ((UserBookingFormData.apply _).tupled, UserBookingFormData.unapply)
  }

  private val userDetails = TableQuery[UserDetailsTable]

  def insertUserDetails(formData: UserBookingFormData): Future[Unit] = db.run {
    userDetails += formData
  }.map(_ => ())
}
