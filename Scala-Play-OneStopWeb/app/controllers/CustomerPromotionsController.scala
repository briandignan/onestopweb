package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.validation.Constraint
import play.api.data.validation.ValidationResult
import play.api.data.validation.Valid
import play.api.data.validation.Invalid
import play.api.data.Forms._
import play.api.templates.HtmlFormat.Appendable

import java.util.Properties

import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

import anorm._

import models._
import views._

import com.techventus.server.voice.Voice;

object CustomerPromotionsController extends Controller with Secured {

	 
	val promotionForm = Form(
		mapping(
			"productType" -> longNumber,
			"messageBody" -> nonEmptyText,
			"gvoice" -> optional(mapping(
				"userName" -> email,
				"password" -> nonEmptyText
			)(Credentials.apply)(Credentials.unapply)),
			"gmail" -> optional(mapping(
				"credentials" -> mapping(
					"userName" -> email,
					"password" -> nonEmptyText
				)(Credentials.apply)(Credentials.unapply),
				"subject" -> nonEmptyText
			)(PromotionMailInfo.apply)(PromotionMailInfo.unapply))
		)(Promotion.apply)(Promotion.unapply)
	)
	
	
	def create() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok( html.customerPromotionCreate( user, promotionForm, ProductType.options ) )
			}.getOrElse( Forbidden )
		}
	}
	
	
	def execute() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				promotionForm.bindFromRequest.fold(
					formWithErrors => BadRequest( html.customerPromotionCreate( user, formWithErrors, ProductType.options ) ),
					promotion => {
						
						if ( promotion.gmailInfo.isEmpty && promotion.gvoiceCred.isEmpty ) {
							// Neither was selected. Go back to the customer list page and send an error
							CustomerManagementController.home.flashing( "error" -> "Promotion failed. No Email or SMS credentials were provided." )
						} 
						
						// Send emails if we were provided with gmail credentials and an email subject
						val emailsSent = promotion.gmailInfo.map { info => 
							// Find all of the customers who want to receive an email for this department
							val emails = Customer.receiveEmailPromotion( promotion.productTypeId )
							sendEmails( info, promotion.message, emails )
							emails.size
						}
						
						// Send text messages if we were provided with gvoice credentials
						val textsSent = promotion.gvoiceCred.map { cred => 
							// Find all of the customers who want to receive a text for this department
							val phoneNumbers = Customer.receiveSmsPromotion( promotion.productTypeId )
							sendTexts( cred, promotion.message, phoneNumbers )
							phoneNumbers.size
						}
						
						val successMessage = {
							if ( emailsSent.isDefined && textsSent.isDefined ) {
								// We sent emails and texts
								emailsSent.get + " email(s) and " + textsSent.get + " text(s) were sent."
								
							} else if ( emailsSent.isDefined ) {
								// We sent just emails
								emailsSent.get + " email(s) were sent."
								
							} else {
								// We sent just text messages
								textsSent.get + " text(s) were sent."
								
							}
						}
						
						CustomerManagementController.home.flashing( "success" -> successMessage )
						//Ok( promotion.toString )
					}
				)
			}.getOrElse( Forbidden )
		}
	}
	
	
	def sendEmails( mailInfo: PromotionMailInfo, body: String, emails: Seq[String] ): Unit = {
		
		val props = new Properties();
		props.put( "mail.smtp.auth", "true" );
		props.put( "mail.smtp.starttls.enable", "true" );
		props.put( "mail.smtp.host", "smtp.gmail.com" );
		props.put( "mail.smtp.port", "587" );

		val session = Session.getInstance( props, new javax.mail.Authenticator() {

			override def getPasswordAuthentication(): PasswordAuthentication = {

				return new PasswordAuthentication( mailInfo.credentials.userName, mailInfo.credentials.password );
			}
		} );

		emails.foreach{ email => 
			val message = new MimeMessage( session );
			message.setFrom( new InternetAddress( "from-email@gmail.com" ) );
			message.setRecipients( Message.RecipientType.TO, email );
			
			message.setSubject( mailInfo.subject );
			message.setText( body );
	
			Transport.send( message );
		}

		
	}
	
	
	def sendTexts( credentials: Credentials, body: String, phoneNumbers: Seq[String] ): Unit = {
		val voice = new Voice( credentials.userName, credentials.password );
		phoneNumbers.foreach { phoneNumber => 
			voice.sendSMS( phoneNumber, body );
		}
	}
	
	
	
		
}