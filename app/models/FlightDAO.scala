package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FlightDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class FlightTable(tag: Tag) extends Table[FlightFormData](tag, "flights") {
    def flightid = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def aircraftId = column[String]("aircraft_id")
    def airlinesName = column[String]("airlines_name")
    def price = column[Int]("price")
    def destination = column[String]("destination")
    def source = column[String]("source")
    def departureTime = column[String]("departure_time")
    def arrivalTime = column[String]("arrival_time")
    def seatAvailability = column[Int]("seat_availability")

    def * = (aircraftId, airlinesName, price, destination, source, departureTime, arrivalTime, seatAvailability) <>
      ((FlightFormData.apply _).tupled, FlightFormData.unapply)
  }

  private val flights = TableQuery[FlightTable]

  def insertFlight(formData: FlightFormData): Future[Unit] = db.run {
    flights += formData
  }.map(_ => ())

  def getFlightsBySourceAndDestination(source: String, destination: String): Future[Seq[FlightFormData]] = db.run {
    flights.filter(f => f.source === source && f.destination === destination).result
  }

  def getAllFlights: Future[Seq[FlightFormData]] = db.run {
    flights.result
  }
}
