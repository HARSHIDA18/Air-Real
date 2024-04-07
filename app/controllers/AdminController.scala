// app/controllers/HomeController.scala

package controllers

import models.{AdminDAO,Admin}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.I18nSupport
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdminController @Inject()(cc: ControllerComponents,adminDao:AdminDAO)(implicit ec: ExecutionContext) extends AbstractController(cc) with I18nSupport {
  val signupAdminForm: Form[Admin] = Form {
    mapping(
      "id"->ignored(0L),
      "username" -> nonEmptyText,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(Admin.apply)(Admin.unapply)
  }

  def showLoginForm(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (request.session.get("userId").isDefined) {
      Ok(views.html.index())
    } else {
      Ok(views.html.adminLogin(signupAdminForm))
    }
  }

  def processLogin(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    signupAdminForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.adminLogin(formWithErrors)))
      },
      loginData => {
        adminDao.getUserByCredentials(loginData.username, loginData.email, loginData.password).map {
          case Some(user) =>
            Ok(views.html.index())
          case None =>
            val formWithErrors = signupAdminForm.withGlobalError("Invalid username/email or password")
            BadRequest(views.html.adminLogin(formWithErrors))
        }
      }
    )
  }

  def showregisterForm: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (request.session.get("userId").isDefined) {
      Ok(views.html.index())
    } else {
      Ok(views.html.adminRegister(signupAdminForm))
    }
  }

  def processRegister: Action[AnyContent] = Action.async { implicit request =>
    signupAdminForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.adminRegister(formWithErrors)))
      },
      adminData => {
        adminDao.addUser(adminData).map { _ =>
          Ok(views.html.adminLogin(signupAdminForm))
            .flashing("success" -> "User registered successfully")
        }
      }
    )
  }
}
