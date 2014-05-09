package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import java.util.Date

case class VendorOrder( orderId: Pk[Long], vendorId: Long, vendorName: String, dateOrdered: Date, dateReceived: Option[Date], itemsOrdered: Long, totalCost: BigDecimal)

object VendorOrder { 
	
	def simpleParser = {
		get[Pk[Long]]( "VendorOrders.VendorOrderID" ) ~
		get[Long]( "VendorOrders.VendorID" ) ~
		get[String]( "Vendors.Name" ) ~
		get[Date]( "VendorOrders.DateOrdered" ) ~
		get[Option[Date]]( "VendorOrders.DateDelivered" ) ~
		get[Long]( "ItemsOrdered" ) ~
		get[java.math.BigDecimal]( "TotalCost" ) map {
			case orderId~vendorId~name~dateOrdered~dateReceived~itemsOrdered~totalCost => VendorOrder( orderId, vendorId, name, dateOrdered, dateReceived, itemsOrdered, totalCost )
		}
	}
	
	def list( vendorId: Long ): List[VendorOrder] = {
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