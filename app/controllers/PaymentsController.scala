package controllers

import javax.inject._
import play.api.mvc._
import play.api.Configuration
import com.stripe.Stripe
import com.stripe.exception.StripeException
import com.stripe.model.PaymentIntent
import com.stripe.param.PaymentIntentCreateParams

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentsController @Inject()(
                                    cc: ControllerComponents,
                                    config: Configuration
                                  )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  private val stripeSecretKey = config.get[String]("stripe.secretKey")
  Stripe.apiKey = stripeSecretKey


  def createPaymentIntent(amount: Int): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val params = new PaymentIntentCreateParams.Builder()
      .setCurrency("usd")
      .setAmount(amount)
      .build()
    val paymentIntent = try {
      PaymentIntent.create(params)
    } catch {
      case e: StripeException => throw new RuntimeException("Error creating payment intent", e)
    }
    Future.successful(Ok(paymentIntent.toJson))
  }
}
