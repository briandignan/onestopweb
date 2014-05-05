package models

import scala.math.BigDecimal

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Vendor( 
		id: Pk[Long], 
		name: String, 
		city: Option[String], 
		state: Option[String], 
		country: Option[String], 
		email: Option[String], 
		phoneOne: Option[String], 
		phoneTwo: Option[String], 
		fax: Option[String],
		items: Seq[VendorItem] )

object Vendor {

	
	val vendorWithoutItems = {
		get[Pk[Long]]( "Vendors.VendorID" ) ~
		get[String]( "Vendors.Name" ) ~
		get[Option[String]]( "Vendors.City" ) ~
		get[Option[String]]( "Vendors.State" ) ~
		get[Option[String]]( "Vendors.Country" ) ~
		get[Option[String]]( "Vendors.EmailAddress" ) ~
		get[Option[String]]( "Vendors.PhoneOne" ) ~
		get[Option[String]]( "Vendors.PhoneTwo" ) ~
		get[Option[String]]( "Vendors.Fax" ) map {
			case id~name~city~state~country~email~phoneOne~phoneTwo~fax => Vendor(id, name, city, state, country, email, phoneOne, phoneTwo, fax, Seq())
		}
	}
	
	
	val vendorJoinVendorItems = {
		get[Pk[Long]]( "Vendors.VendorID" ) ~
		get[String]( "Vendors.Name" ) ~
		get[Option[String]]( "Vendors.City" ) ~
		get[Option[String]]( "Vendors.State" ) ~
		get[Option[String]]( "Vendors.Country" ) ~
		get[Option[String]]( "Vendors.EmailAddress" ) ~
		get[Option[String]]( "Vendors.PhoneOne" ) ~
		get[Option[String]]( "Vendors.PhoneTwo" ) ~
		get[Option[String]]( "Vendors.Fax" ) ~
		get[Option[Long]]( "VendorItems.ItemID" ) ~
		get[Option[java.math.BigDecimal]]( "VendorItems.UnitCost" ) map {
			case id~name~city~state~country~email~phoneOne~phoneTwo~fax~itemId~unitCost => ((id, name, city, state, country, email, phoneOne, phoneTwo, fax), (itemId, unitCost.map(BigDecimal(_))) )
		}
	}
	
	def findById( id: Long ): Option[Vendor] = {
		DB.withConnection { implicit connection => 
			val result = SQL(
				"""
					SELECT Vendors.*, VendorItems.ItemID, VendorItems.UnitCost FROM Vendors
					LEFT JOIN VendorItems ON Vendors.VendorID=VendorItems.VendorID
					WHERE Vendors.VendorID = {vendorId}
				"""
			).on(
				'vendorId -> id
			).as( vendorJoinVendorItems * )
			
			
			val groupedByBaseVendor = result.groupBy( tuple => tuple._1 )
			val vendorList = groupedByBaseVendor.map { case (base, rest) =>
				// Create a list of items associated to this Vendor
				val items = rest.unzip._2.foldLeft(List(): List[VendorItem]) { (acc, item) => if ( item._1 == None ) acc else acc :+ VendorItem(item._1.get, item._2.get)}
				Vendor( base._1, base._2, base._3, base._4, base._5, base._6, base._7, base._8, base._9, items )
			}
			vendorList.headOption
		}
	}
	
	def findAll(): Seq[Vendor] = {
		DB.withConnection { implicit connection => 
			SQL(
				"""
					SELECT * From Vendors
				"""
			).as( vendorWithoutItems * )
		}
	}
	
	def create( vendor: Vendor ): Int = {
		0
	}
	
	def update( id: Long, vendor: Vendor ): Unit = {
		println( vendor) 
		DB.withConnection { implicit connection =>
			SQL(
				"""
					UPDATE Vendors
					SET Name={name}, City={city}, State={state}, Country={country}, EmailAddress={email}, PhoneOne={phoneOne}, PhoneTwo={phoneTwo}, Fax={fax}
					WHERE VendorID={id}
				"""
			).on(
				'id -> id,
				'name -> vendor.name,
				'city -> vendor.city,
				'state -> vendor.state,
				'country -> vendor.country,
				'email -> vendor.email,
				'phoneOne -> vendor.phoneOne,
				'phoneTwo -> vendor.phoneTwo,
				'fax -> vendor.fax
			).executeUpdate
			
			
			val existingItems = SQL( 
				"""
					SELECT ItemID, UnitCost FROM VendorItems
					WHERE VendorID={vendorId}
				"""
			).on( 
				'vendorId -> id
			).as( VendorItem.simpleParser *).toSet
			
			val formItems = vendor.items.toSet
			
			
			// Add VendorItem records where an ItemID exists in the submitted form but not in the database
			formItems.diff( existingItems ).foreach { item =>
				SQL(
					"""
						INSERT INTO VendorItems
						(VendorID, ItemID, UnitCost) VALUES
						({vendorId}, {itemId}, {unitCost})
					"""
				).on( 
					'vendorId -> id,
					'itemId -> item.itemId,
					'unitCost -> item.unitCost.toString
				).executeUpdate
			}
			
			// Remove VendorItem records where an ItemID exists in the database but not the submitted form
			existingItems.diff( formItems ).foreach { item => 
				SQL(
					"""
						DELETE FROM VendorItems
						WHERE VendorID={vendorId}
						AND ItemID={itemId}
					"""	
				).on(
					'vendorId -> id,
					'itemId -> item.itemId
				).executeUpdate
			}
			
			
		}
	}
	
	def delete( id: Long ): Int = {
		0
	}
	
}