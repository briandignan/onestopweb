package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Customer( id: Pk[Long], firstName: String, lastName: String, email: Option[String], phoneNumber: Option[String], promotionDeviceId: Long )

object Customer {

	val simpleParser = 
		get[Pk[Long]]( "Customers.CustomerID" ) ~
		get[String]( "Customers.FirstName" ) ~
		get[String]( "Customers.LastName" ) ~
		get[Option[String]]( "Customers.EmailAddress" ) ~
		get[Option[String]]( "Customers.PhoneNumber" ) ~ 
		get[Long]( "Customers.PromotionDeviceID" ) map {
			case id ~ firstName ~ lastName ~ email ~ phone ~ promotionDeviceId => Customer( id, firstName, lastName, email, phone, promotionDeviceId )
		}
		
	val withPromotionDevice = 
		Customer.simpleParser ~ PromotionDevice.simpleParser map {
			case customer ~ promotionDevice => ( customer, promotionDevice )
		}
		
	
	def findAll(): Seq[(Customer,PromotionDevice)] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					SELECT * FROM Customers 
					JOIN PromotionDevices ON Customers.PromotionDeviceID=PromotionDevices.PromotionDeviceID
				"""
			).as( Customer.withPromotionDevice *)
		}
	}
	
	def findById(id: Long): Option[Customer] = {
		DB.withConnection{ implicit connection => 
			SQL(
				"""
					SELECT * FROM Customers 
					WHERE CustomerID = {id}
				"""
			).on(
				'id -> id
			).as( Customer.simpleParser.singleOpt )
		}
	}
	
}