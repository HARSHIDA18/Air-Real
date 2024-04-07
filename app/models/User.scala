package models

case class User (
                  id:Long=0,
                  username: String,
                  email:String,
                  password: String
                )