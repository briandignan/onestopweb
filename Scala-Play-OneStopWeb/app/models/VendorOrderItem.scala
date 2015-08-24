package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import java.util.Date

import scala.language.postfixOps

case class VendorOrderItem( sku: String, description: String, quantity: Long, unitCost: BigDecimal ) { 
	val totalCost = quantity * unitCost
}

object VendorOrderItem {
	
	val simpleParser = {
		get[String]( "Inventory.SKU" ) ~
		get[String]( "Inventory.Description" ) ~
		get[Long]( "VendorOrderItems.QuantityOrdered" ) ~
		get[java.math.BigDecimal]( "VendorOrderItems.UnitCost" ) map {
			case sku~description~quantity~unitCost => VendorOrderItem( sku, description, quantity, unitCost )
		}
	}
	
	
	def list( orderId: Long ): List[VendorOrderItem] = {
		
		DB.withConnection { implicit connection => 
			SQL(
				"""
					SELECT Inventory.SKU, Inventory.Description, VendorOrderItems.QuantityOrdered, VendorOrderItems.UnitCost 
					FROM VendorOrders 
					JOIN VendorOrderItems ON VendorOrderItems.VendorOrderID=VendorOrders.VendorOrderID 
					JOIN Inventory ON Inventory.ItemID=VendorOrderItems.ItemID 
					WHERE VendorOrders.VendorOrderID={orderId}
				"""
			).on(
				'orderId -> orderId
			).as( simpleParser * )	
		}
		
	}
}