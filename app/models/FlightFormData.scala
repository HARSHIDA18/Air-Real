package models

case class FlightFormData(aircraftId: String, airlinesName: String, price:Int, destination: String, source: String, departureTime: String, arrivalTime: String, seatAvailability: Int)
