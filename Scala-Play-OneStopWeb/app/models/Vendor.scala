package models

import scala.math.BigDecimal

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

import scala.language.postfixOps

case class Vendor( 
		id: Option[Long], 
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
		get[Option[Long]]( "Vendors.VendorID" ) ~
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
		get[Option[Long]]( "Vendors.VendorID" ) ~
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
	
	def create( vendor: Vendor ): Unit = {
		DB.withConnection { implicit connection =>
			val vendorId = SQL(
				"""
					INSERT INTO Vendors
					(Name, City, State, Country, EmailAddress, PhoneOne, PhoneTwo, Fax)
					VALUES
					({name}, {city}, {state}, {country}, {email}, {phoneOne}, {phoneTwo}, {fax})
				"""
			).on(
				'name -> vendor.name,
				'city -> vendor.city,
				'state -> vendor.state,
				'country -> vendor.country,
				'email -> vendor.email,
				'phoneOne -> vendor.phoneOne,
				'phoneTwo -> vendor.phoneTwo,
				'fax -> vendor.fax
			).executeInsert( scalar[Long] single )
			
			println( "Vendor inserted with ID %d".format( vendorId ) )
			
			
			val rowsInserted = vendor.items.map { item => 
				SQL( 
					"""
						INSERT INTO VendorItems
						(VendorID, ItemID, UnitCost)
						VALUES
						({vendorId}, {itemId}, {unitCost})
					"""
				).on( 
					'vendorId -> vendorId,
					'itemId -> item.itemId,
					'unitCost -> item.unitCost.toString
				).executeUpdate
			}
			
			println( "Inserted %d VendorItems rows for new Vendor.".format( rowsInserted.size ) )
			
		}
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
			).as( VendorItem.simpleParser *)
			
			val formItems = vendor.items
			
			val existingItemSet = existingItems.toSet
			
			val existingItemIds = existingItems.map { item => 
				item.itemId
			}.toSet
			
			val formItemIds = formItems.map { item =>
				item.itemId
			}.toSet
			
			
			
			formItems.foreach { item =>
				// Add VendorItem records where an ItemID exists in the submitted form but not in the database
				if ( !existingItemIds.contains(item.itemId) ) {
					println( "Deleting Item. ItemID: %d VendorID: %d UnitCost: %s".format( item.itemId, id, item.unitCost ) )
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
				
				// Update VendorItem records where the item previously existed for this vendor, but the UnitCost has been updated
				if ( existingItemIds.contains(item.itemId) && !existingItemSet.contains(item) ) {
					println( "Updating Item. ItemID: %d VendorID: %d UnitCost: %s".format( item.itemId, id, item.unitCost ) )
					SQL(
						"""
							UPDATE VendorItems
							SET UnitCost={unitCost}
							WHERE VendorID={vendorId}
							AND ItemID={itemId}
						"""
					).on(
						'unitCost -> item.unitCost.toString,
						'itemId -> item.itemId,
						'vendorId -> id
					).executeUpdate
				}
				
				
			}
			
			
			// Remove VendorItem records where an ItemID exists in the database but not the submitted form
			existingItems.foreach { item =>
				if ( !formItemIds.contains(item.itemId) ) {
					println( "Inserting Item. ItemID: %d VendorID: %d UnitCost: %s".format( item.itemId, id, item.unitCost ) )
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
			
			/*
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
			*/
			
		}
	}
	
	def delete( id: Long ): Int = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					DELETE FROM VendorItems
					WHERE VendorID={vendorId}
				"""
			).on(
				'vendorId -> id
			).executeUpdate
			
			SQL(
				"""
					DELETE FROM Vendors
					WHERE VendorID={vendorId}
				"""
			).on(
				'vendorId -> id
			).executeUpdate
			
		}
	}
	
	val itemsOfferedParser = {
		get[Long]( "VendorItems.ItemID" ) ~
		get[String]( "Inventory.Description" ) ~
		get[java.math.BigDecimal]( "VendorItems.UnitCost" ) map {
			case itemId~description~unitCost => ( itemId.toString, description, BigDecimal(unitCost) )
		}
	}
	
	def itemsOffered( vendorId: Long ): Seq[(String, String, BigDecimal)] = {
		DB.withConnection { implicit connection => 
			SQL(
			"""
				SELECT VendorItems.ItemID, Inventory.Description, VendorItems.UnitCost
				FROM VendorItems
				JOIN Inventory ON VendorItems.ItemID=Inventory.ItemID
				WHERE VendorItems.VendorID={vendorId}
			"""
			).on( 
				'vendorId -> vendorId
			).as( itemsOfferedParser * )
		}
	}
	
	
}