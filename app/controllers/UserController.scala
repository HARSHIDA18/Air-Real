
package controllers


import models.{User, UserDAO}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.I18nSupport
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(cc: ControllerComponents, userDao: UserDAO)(implicit ec: ExecutionContext) extends AbstractController(cc) with I18nSupport {


  // Define a form mapping for the signup data
  val signupForm: Form[User] = Form {
    mapping(
      "id"->ignored(0L),
      "username" -> nonEmptyText,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(User.apply)(User.unapply)
  }

  def showLoginForm(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (request.session.get("userId").isDefined) {
      Ok(views.html.index())
    } else {
      Ok(views.html.userLogin(signupForm))
    }
  }

  def processLogin(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    signupForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.userLogin(formWithErrors)))
      },
      loginData => {
        userDao.getUserByCredentials(loginData.username, loginData.email, loginData.password).map {
          case Some(user) =>
            Ok(views.html.index())
          case None =>
            val formWithErrors = signupForm.withGlobalError("Invalid username/email or password")
            BadRequest(views.html.userLogin(formWithErrors))
        }
      }
    )
  }

  def showregisterForm: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (request.session.get("userId").isDefined) {
      Ok(views.html.index())
    } else {
      Ok(views.html.register(signupForm))
    }
  }

  def processRegister: Action[AnyContent] = Action.async { implicit request =>
    signupForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.register(formWithErrors)))
      },
      userData => {
        userDao.addUser(userData).map { _ =>
          Ok(views.html.userLogin(signupForm))
            .flashing("success" -> "User registered successfully")
        }
      }
    )
  }
}
