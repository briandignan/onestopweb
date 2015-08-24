package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class OrdersByVendor( vendorId: Option[Long], vendorName: String, itemsOffered: Long, lowQuantity: Long, ordersReceived: Long, ordersProcessing: Long )


object OrdersByVendor {

	val vendorIdNameParser = {
		get[Option[Long]]( "Vendors.VendorID" ) ~
		get[String]( "Vendors.Name" ) map {
			case id~name => ( ( id, name ) )
		}
	}
	
	def list(): List[OrdersByVendor] = {
		DB.withConnection { implicit connection =>
			val vendors = SQL(
				"""
					SELECT VendorID, Name FROM Vendors
				"""
			).as( vendorIdNameParser * )
			
			vendors.map { case (vendorId, name) =>
				val itemsOffered = SQL(
					"""
						SELECT count(*) FROM VendorItems
						WHERE VendorID={vendorId}
					"""
				).on( 
					'vendorId -> vendorId.get
				).as( scalar[Long] single )
				
				val lowQuantity = SQL(
					"""
						SELECT count(*) FROM VendorItems
						JOIN Inventory ON VendorItems.ItemID=Inventory.ItemID
						WHERE VendorItems.VendorID={vendorId}
						AND Inventory.QuantityOnHand < Inventory.QuantityLowPoint
					"""
				).on(
					'vendorId -> vendorId.get
				).as( scalar[Long] single )
				
				val ordersReceived = SQL(
					"""
						SELECT count(*) FROM VendorOrders
						WHERE VendorID={vendorId}
						AND DateDelivered IS NOT null
					"""
				).on( 
					'vendorId -> vendorId.get
				).as( scalar[Long] single )
				
				val ordersProcessing = SQL(
					"""
						SELECT count(*) from VendorOrders
						WHERE VendorID={vendorId}
						AND DateDelivered IS null
					"""
				).on(
					'vendorId -> vendorId.get
				).as( scalar[Long] single )
				
				
				OrdersByVendor( vendorId, name, itemsOffered, lowQuantity, ordersReceived, ordersProcessing )
				
			}
		}
	}
}