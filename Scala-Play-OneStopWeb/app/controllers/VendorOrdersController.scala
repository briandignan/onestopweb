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
import anorm._
import models._
import views._
import java.util.Date

import java.util.Properties

import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object VendorOrdersController extends Controller with Secured {

	
	val defaultVendorOrderData = VendorOrder( new Date(), None, None, Seq() )
	
	
	val vendorOrderForm: Form[VendorOrder] = Form(
		mapping(
			"dateOrdered" -> date("MM/dd/yyyy"),
			"dateReceived" -> optional(date("MM/dd/yyyy")),
			"emailInfo" -> optional(mapping(
				"gmailAccount" -> email,
				"gmailPassword" -> nonEmptyText,
				"emailSubject" -> nonEmptyText,
				"emailBody" -> nonEmptyText
			)(EmailInfo.apply)(EmailInfo.unapply)),
			"items" -> seq(
				mapping(
					"itemId" -> longNumber,
					"unitCost" -> bigDecimal,
					"quantity" -> longNumber
				)(VendorPurchaseItem.apply)(VendorPurchaseItem.unapply)
			)
		)(VendorOrder.apply)(VendorOrder.unapply)
	)
	
	
	def list = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok( html.vendorOrdersList( user, OrdersByVendor.list ) )
			}.getOrElse( Forbidden )
		}
	}
	
	
	def vendor( id: Long ) = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map { user =>
				Vendor.findById( id ).map { vendor =>
					Ok( html.vendorSpecificOrdersList( user, vendor, VendorOrderSummary.list( id ) ) )
				}.getOrElse( NotFound )
			}.getOrElse( Forbidden )
		}
	}
	
	
	def editOrder( vendorId: Long, orderId: Long ) = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map { user =>
				Vendor.findById( vendorId ).map { vendor =>
					VendorOrder.findById( orderId ).map { order =>
						val itemsOfferedWithPrices = Vendor.itemsOffered( vendorId )
						val itemsOffered = itemsOfferedWithPrices.map{ 
							case ( itemId, description, price ) => ( itemId, description )
						}
						val itemPrices = itemsOfferedWithPrices.map{
							case( itemId, description, price ) => ( itemId, price.toString ) 
						}
						Ok( html.vendorSpecificOrder( user, orderId, vendor, vendorOrderForm.fill( order ), itemsOffered, itemPrices ) )
					
					}.getOrElse( NotFound )
				}.getOrElse( NotFound )
			}.getOrElse( Forbidden )
		}
	}
	
	
	def updateOrder( vendorId: Long, orderId: Long ) = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map { user =>
				vendorOrderForm.bindFromRequest.fold(
					formWithErrors => {
						Vendor.findById( vendorId ).map { vendor =>
							val itemsOfferedWithPrices = Vendor.itemsOffered( vendorId )
							val itemsOffered = itemsOfferedWithPrices.map{ 
								case ( itemId, description, price ) => ( itemId, description )
							}
							val itemPrices = itemsOfferedWithPrices.map{
								case( itemId, description, price ) => ( itemId, price.toString ) 
							}
							BadRequest( html.vendorSpecificOrder( user, orderId, vendor, formWithErrors, itemsOffered, itemPrices ) )
						}.getOrElse( NotFound )
					},
					orderToUpdate => {
						VendorOrder.update( orderId, orderToUpdate )
						Redirect( routes.VendorOrdersController.vendor(vendorId) ).flashing( "success" -> "Order updated" )
					}
				)
			}.getOrElse( Forbidden )
		}
	}
	
	
	def createOrder( vendorId: Long ) = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map { user =>
				Vendor.findById( vendorId ).map{ vendor =>
					val itemsOfferedWithPrices = Vendor.itemsOffered( vendorId )
					val itemsOffered = itemsOfferedWithPrices.map{ 
						case ( itemId, description, price ) => ( itemId, description )
					}
					val itemPrices = itemsOfferedWithPrices.map{
						case( itemId, description, price ) => ( itemId, price.toString ) 
					}
					Ok( html.vendorGenerateProductOrder( user, vendor, vendorOrderForm.fill( defaultVendorOrderData ), itemsOffered, itemPrices ) )
				}.getOrElse( NotFound )
			}.getOrElse( Forbidden )
		}
	}
	
	
	def executeOrder( vendorId: Long ) = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map { user =>
				vendorOrderForm.bindFromRequest.fold(
					formWithErrors => {
						Vendor.findById( vendorId ).map{ vendor =>
							val itemsOfferedWithPrices = Vendor.itemsOffered( vendorId )
							val itemsOffered = itemsOfferedWithPrices.map{ 
								case ( itemId, description, price ) => ( itemId, description )
							}
							val itemPrices = itemsOfferedWithPrices.map{
								case( itemId, description, price ) => ( itemId, price.toString ) 
							}
							BadRequest( html.vendorGenerateProductOrder( user, vendor, formWithErrors, itemsOffered, itemPrices ) )
						}.getOrElse( NotFound )
					},
					vendorOrder => {
						// The form passed validation. 
						vendorOrder.emailInfo.map { emailInfo =>
							// Send the email
							Vendor.findById(vendorId) map { vendor => 
							
								vendor.email map { emailAddress =>
									val props = new Properties();
									props.put( "mail.smtp.auth", "true" );
									props.put( "mail.smtp.starttls.enable", "true" );
									props.put( "mail.smtp.host", "smtp.gmail.com" );
									props.put( "mail.smtp.port", "587" );
							
									val session = Session.getInstance( props, new javax.mail.Authenticator() {
							
										override def getPasswordAuthentication(): PasswordAuthentication = {
							
											return new PasswordAuthentication( emailInfo.gmailAccount, emailInfo.gmailPassword );
										}
									} );
							
									val message = new MimeMessage( session );
									message.setFrom( new InternetAddress( "from-email@gmail.com" ) );
									message.setRecipients( Message.RecipientType.TO, emailAddress );
									
									message.setSubject( emailInfo.subject );
									message.setText( emailInfo.body );
						
									Transport.send( message );
								}
							}
						}
						
						// Add the info to the database
						VendorOrder.add( vendorId, vendorOrder )
						
						Redirect( routes.VendorOrdersController.vendor(vendorId) ).flashing( "success" -> "Order executed" )
					}
				)
			}.getOrElse( Forbidden )
		}
	}
	
	
	
	
}