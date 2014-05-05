package models

import scala.math.BigDecimal


import anorm.SqlParser._
import anorm._

case class VendorItem( itemId: Long, unitCost: BigDecimal )

object VendorItem {
	
	val simpleParser = {
		get[Long]( "VendorItems.ItemID" ) ~
		get[java.math.BigDecimal]( "VendorItems.UnitCost" ) map {
			case itemId~unitCost => VendorItem( itemId, BigDecimal(unitCost) ) 
		}
	}
	
}