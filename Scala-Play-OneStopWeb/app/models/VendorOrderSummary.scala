package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import java.util.Date
import java.text.SimpleDateFormat

import scala.language.postfixOps

case class VendorOrderSummary( orderId: Option[Long], vendorId: Long, vendorName: String, dateOrdered: String, dateReceived: Option[String], itemsOrdered: String, totalCost: String)

object VendorOrderSummary { 
	
	val dateFormat = new SimpleDateFormat("MM/dd/yyyy")
	
	def simpleParser = {
		get[Option[Long]]( "VendorOrders.VendorOrderID" ) ~
		get[Long]( "VendorOrders.VendorID" ) ~
		get[String]( "Vendors.Name" ) ~
		get[Date]( "VendorOrders.DateOrdered" ) ~
		get[Option[Date]]( "VendorOrders.DateDelivered" ) ~
		get[java.math.BigDecimal]( "ItemsOrdered" ) ~ // Breaks H2 but fixes an issue with MySQL (Changed from Long to BigDecimal)
		get[java.math.BigDecimal]( "TotalCost" ) map {
			case orderId~vendorId~name~dateOrdered~dateReceived~itemsOrdered~totalCost => {
				val formatOrdered = dateFormat.format( dateOrdered )
				val formatReceived = dateReceived.map { date => 
					dateFormat.format( date )
				}
				VendorOrderSummary( orderId, vendorId, name, formatOrdered, formatReceived, itemsOrdered.toString, totalCost.toString )
			}
		}
	}
	
	def list( vendorId: Long ): List[VendorOrderSummary] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					SELECT VendorOrders.VendorOrderID, VendorOrders.VendorID, Vendors.Name, DateOrdered, DateDelivered, sum(QuantityOrdered) AS ItemsOrdered, sum(UnitCost*QuantityOrdered) AS TotalCost 
					FROM VendorOrders 
					JOIN VendorOrderItems ON VendorOrders.VendorOrderID=VendorOrderItems.VendorOrderID 
					JOIN Vendors ON VendorOrders.VendorID=Vendors.VendorID 
					WHERE VendorOrders.VendorID={vendorId}
					GROUP BY VendorOrders.VendorOrderID, VendorOrders.VendorID, Vendors.Name, DateOrdered, DateDelivered
				"""
			).on( 
				'vendorId -> vendorId
			).as( simpleParser * )
		}
	}
}