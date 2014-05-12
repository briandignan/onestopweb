package models

import scala.math.BigDecimal
import anorm.SqlParser._
import anorm._
import play.api.db._
import play.api.Play.current

case class VendorPurchaseItem( itemId: Long, unitCost: BigDecimal, quantity: Long )

object VendorPurchaseItem {
	
	val simpleParser = {
		get[Long]( "VendorOrderItems.ItemID" ) ~
		get[java.math.BigDecimal]( "VendorOrderItems.UnitCost" ) ~
		get[Long]( "VendorOrderItems.QuantityOrdered" ) map {
			case itemId~unitCost~quantity => VendorPurchaseItem( itemId, BigDecimal(unitCost), quantity ) 
		}
	}
	
	
	def findById( orderId: Long ): Seq[VendorPurchaseItem] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					SELECT ItemID, QuantityOrdered, UnitCost
					FROM VendorOrderItems
					WHERE VendorOrderID={orderId}
				"""
			).on( 
				'orderId -> orderId
			).as( simpleParser.* )
		}
	}
	
}