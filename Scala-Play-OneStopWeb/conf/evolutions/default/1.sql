# --- First database schema

# --- !Ups

CREATE TABLE AdminUsers (
  UserID INT UNSIGNED NOT NULL AUTO_INCREMENT,
  EmailAddress VARCHAR(255) NOT NULL,
  FirstName VARCHAR(255) NOT NULL,
  LastName VARCHAR(255) NOT NULL,
  Password VARCHAR(255) NOT NULL,
  PRIMARY KEY (UserID)
);

CREATE UNIQUE INDEX  AdminUsers_Email_Index ON AdminUsers (EmailAddress);

/* Test records inserted via Global class. Required because of password salting */

CREATE TABLE ProductType(
	ProductTypeID INT UNSIGNED NOT NULL AUTO_INCREMENT,
	Name VARCHAR(255) NOT NULL,
	PRIMARY KEY (ProductTypeID)
);

CREATE UNIQUE INDEX  ProductType_Name_Index ON ProductType (Name);

INSERT INTO ProductType (Name) VALUES ('Drinks');
INSERT INTO ProductType (Name) VALUES ('Ice Cream');
INSERT INTO ProductType (Name) VALUES ('Frozen Foods');
INSERT INTO ProductType (Name) VALUES ('Tobacco');
INSERT INTO ProductType (Name) VALUES ('Pharmacy');
INSERT INTO ProductType (Name) VALUES ('Household');
INSERT INTO ProductType (Name) VALUES ('Confections');
INSERT INTO ProductType (Name) VALUES ('Snacks');
INSERT INTO ProductType (Name) VALUES ('School Supplies');
INSERT INTO ProductType (Name) VALUES ('Toiletries');
INSERT INTO ProductType (Name) VALUES ('Chips');
INSERT INTO ProductType (Name) VALUES ('Party Supplies');
INSERT INTO ProductType (Name) VALUES ('Misc');
INSERT INTO ProductType (Name) VALUES ('Lottery');

/* 
 * Doesn't contain UnitCost because the cost for a single item could vary from
 * vendor to vendor. 
 */
CREATE TABLE Inventory(
	ItemID INT UNSIGNED NOT NULL AUTO_INCREMENT,
	ProductTypeID INT UNSIGNED NOT NULL,
	SKU VARCHAR(255) NOT NULL,
	Description VARCHAR(255) NOT NULL,
	UnitPrice DECIMAL(10,2) NOT NULL,
	QuantityOnHand INT UNSIGNED NOT NULL,
	QuantityPerOrder INT UNSIGNED NOT NULL,
	QuantityLowPoint INT UNSIGNED NOT NULL, 
	PRIMARY KEY (ItemID)
);

ALTER TABLE Inventory ADD CONSTRAINT FK_Inventory_ProductType FOREIGN KEY (ProductTypeID) REFERENCES ProductType (ProductTypeID) ON DELETE RESTRICT ON UPDATE CASCADE;
CREATE UNIQUE INDEX  Inventory_SKU_Index ON Inventory (SKU);



INSERT INTO Inventory (ProductTypeID, SKU, Description, UnitPrice, QuantityOnHand, QuantityPerOrder, QuantityLowPoint) SELECT ProductTypeID, '714415094706','Pong Ballz','2.99','0','38','18' FROM ProductType WHERE Name = 'Party Supplies';
INSERT INTO Inventory (ProductTypeID, SKU, Description, UnitPrice, QuantityOnHand, QuantityPerOrder, QuantityLowPoint) SELECT ProductTypeID, '644209412006','Duncan Hines Red Velvet','3.49','6','22','11' FROM ProductType WHERE Name = 'Household';
INSERT INTO Inventory (ProductTypeID, SKU, Description, UnitPrice, QuantityOnHand, QuantityPerOrder, QuantityLowPoint) SELECT ProductTypeID, '716165152804','Raw Roller 110 mm','7.99','7','20','11' FROM ProductType WHERE Name = 'Tobacco';
INSERT INTO Inventory (ProductTypeID, SKU, Description, UnitPrice, QuantityOnHand, QuantityPerOrder, QuantityLowPoint) SELECT ProductTypeID, '716165177814','Elements King Size Papers 32 ct','3.99','53','2','1' FROM ProductType WHERE Name = 'Tobacco';
INSERT INTO Inventory (ProductTypeID, SKU, Description, UnitPrice, QuantityOnHand, QuantityPerOrder, QuantityLowPoint) SELECT ProductTypeID, '716165200109','Skunk Hawaiian Skunk 32 ct','3.49','0','22','3' FROM ProductType WHERE Name = 'Tobacco';
INSERT INTO Inventory (ProductTypeID, SKU, Description, UnitPrice, QuantityOnHand, QuantityPerOrder, QuantityLowPoint) SELECT ProductTypeID, '716165179481','Raw Pre-Rolled Tips','2.29','22','37','32' FROM ProductType WHERE Name = 'Tobacco';



CREATE TABLE Vendors(
	VendorID INT UNSIGNED NOT NULL AUTO_INCREMENT,
	Name VARCHAR(255) NOT NULL,
	City VARCHAR(255),
	State VARCHAR(255),
	Country VARCHAR(255),
	EmailAddress VARCHAR(255),
	PhoneOne VARCHAR(255),
	PhoneTwo VARCHAR(255),
	Fax VARCHAR(255),
	PRIMARY KEY (VendorID)
);

CREATE UNIQUE INDEX  Vendors_Name_Index ON Vendors (Name);


/* Contains which vendors offer which items for sale, and at what cost */
CREATE TABLE VendorItems ( 
	VendorID INT UNSIGNED NOT NULL,
	ItemID INT UNSIGNED NOT NULL,
	UnitCost DECIMAL(10,2) NOT NULL,
	PRIMARY KEY (VendorID, ItemID)
);

ALTER TABLE VendorItems ADD CONSTRAINT FK_VendorItems_Inventory FOREIGN KEY (ItemID) REFERENCES Inventory (ItemID) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE VendorItems ADD CONSTRAINT FK_VendorItems_Vendor FOREIGN KEY (VendorID) REFERENCES Vendors (VendorID) ON DELETE RESTRICT ON UPDATE CASCADE;


CREATE TABLE PromotionDevices(
	PromotionDeviceID INT UNSIGNED NOT NULL,
	Name VARCHAR(255) NOT NULL,
	PRIMARY KEY (PromotionDeviceID)
);

CREATE UNIQUE INDEX  PromotionDevices_Name_Index ON PromotionDevices (Name);

INSERT INTO PromotionDevices (PromotionDeviceID, Name) VALUES (1, 'Not active');
INSERT INTO PromotionDevices (PromotionDeviceID, Name) VALUES (2, 'Phone');
INSERT INTO PromotionDevices (PromotionDeviceID, Name) VALUES (3, 'Email');
INSERT INTO PromotionDevices (PromotionDeviceID, Name) VALUES (4, 'Phone and Email');


CREATE TABLE Customers(
	CustomerID INT UNSIGNED NOT NULL AUTO_INCREMENT,
	FirstName VARCHAR(255) NOT NULL,
	LastName VARCHAR(255) NOT NULL,
	PhoneNumber VARCHAR(255),
	EmailAddress VARCHAR(255),
	PromotionDeviceID INT UNSIGNED NOT NULL DEFAULT 0, /* 1 if no promotion, 2 for phone, 3 for email, 4 for both */
	PRIMARY KEY (CustomerID)
);

ALTER TABLE Customers ADD CONSTRAINT FK_Customers_PromotionDevices FOREIGN KEY (PromotionDeviceID) REFERENCES PromotionDevices (PromotionDeviceID) ON DELETE RESTRICT ON UPDATE CASCADE;
CREATE UNIQUE INDEX  Customers_PhoneNumber_Index ON Customers (PhoneNumber);
CREATE UNIQUE INDEX  Customers_EmailAddress_Index ON Customers (EmailAddress);


INSERT INTO Customers (CustomerID, FirstName, LastName, PhoneNumber, EmailAddress, PromotionDeviceID) VALUES (1, 'George', 'Washington', null, null, 1);
INSERT INTO Customers (CustomerID, FirstName, LastName, PhoneNumber, EmailAddress, PromotionDeviceID) VALUES (2, 'Thomas', 'Jefferson', '3013213213', 'thomas.jefferson@presidents.com', 4);
INSERT INTO Customers (CustomerID, FirstName, LastName, PhoneNumber, EmailAddress, PromotionDeviceID) VALUES (3, 'John', 'Adams', null, 'john.adams@presidents.com', 1);
INSERT INTO Customers (CustomerID, FirstName, LastName, PhoneNumber, EmailAddress, PromotionDeviceID) VALUES (4, 'Abraham', 'Lincoln', '4101234567', 'abe.lincoln@presidents.com', 2);
INSERT INTO Customers (CustomerID, FirstName, LastName, PhoneNumber, EmailAddress, PromotionDeviceID) VALUES (5, 'Teddy', 'Roosevelt', null, 'teddy.roosevelt@presidents.com', 3);


CREATE TABLE CustomerOrders(
	CustomerOrderID INT UNSIGNED NOT NULL AUTO_INCREMENT,
	/* CustomerID is nullable because most customers will not be tracked by this system */
	CustomerID INT UNSIGNED,
	DateTime TIMESTAMP NOT NULL,
	PRIMARY KEY (CustomerOrderID)
);

ALTER TABLE CustomerOrders ADD CONSTRAINT FK_CustomerOrders_Customers FOREIGN KEY (CustomerID) REFERENCES Customers (CustomerID) ON DELETE RESTRICT ON UPDATE CASCADE;


INSERT INTO CustomerOrders (CustomerOrderID, CustomerID, DateTime) VALUES (1, null, '2014-04-22 13:30:05');
INSERT INTO CustomerOrders (CustomerOrderID, CustomerID, DateTime) VALUES (2, 1, '2014-04-30 16:30:00');


CREATE TABLE CustomerOrderItems(
	CustomerOrderID INT UNSIGNED NOT NULL,
	ItemID INT UNSIGNED NOT NULL,
	QuantityOrdered INT UNSIGNED NOT NULL,
	/* Unit price exists in Items, but it's also here because pricing will vary over time
	 * and the pricing change in the Items table shouldn't impact previous sales. 
	 */
	UnitPrice DECIMAL(10,2) NOT NULL, 
	PRIMARY KEY (CustomerOrderID, ItemID)
);

ALTER TABLE CustomerOrderItems ADD CONSTRAINT FK_CustomerOrderItems_CustomerOrders FOREIGN KEY (CustomerOrderID) REFERENCES CustomerOrders (CustomerOrderID) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE CustomerOrderItems ADD CONSTRAINT FK_CustomerOrderItems_Inventory FOREIGN KEY (ItemID) REFERENCES Inventory (ItemID) ON DELETE RESTRICT ON UPDATE CASCADE;

INSERT INTO CustomerOrderItems (CustomerOrderID, ItemID, QuantityOrdered, UnitPrice) VALUES (1, (SELECT ItemID FROM Inventory WHERE SKU = '714415094706'), 5, 2.99);
INSERT INTO CustomerOrderItems (CustomerOrderID, ItemID, QuantityOrdered, UnitPrice) VALUES (1, (SELECT ItemID FROM Inventory WHERE SKU = '644209412006'), 2, 3.75);
INSERT INTO CustomerOrderItems (CustomerOrderID, ItemID, QuantityOrdered, UnitPrice) VALUES (1, (SELECT ItemID FROM Inventory WHERE SKU = '716165152804'), 1, 7.99);
INSERT INTO CustomerOrderItems (CustomerOrderID, ItemID, QuantityOrdered, UnitPrice) VALUES (2, (SELECT ItemID FROM Inventory WHERE SKU = '716165177814'), 1, 3.99);
INSERT INTO CustomerOrderItems (CustomerOrderID, ItemID, QuantityOrdered, UnitPrice) VALUES (2, (SELECT ItemID FROM Inventory WHERE SKU = '716165200109'), 1, 3.49);
INSERT INTO CustomerOrderItems (CustomerOrderID, ItemID, QuantityOrdered, UnitPrice) VALUES (2, (SELECT ItemID FROM Inventory WHERE SKU = '716165179481'), 2, 2.29);



CREATE TABLE CustomerFavorites(
	CustomerID INT UNSIGNED NOT NULL,
	ProductTypeID INT UNSIGNED NOT NULL,
	PRIMARY KEY (CustomerID, ProductTypeID)
);

ALTER TABLE CustomerFavorites ADD CONSTRAINT FK_CustomerFavorites_Customers FOREIGN KEY (CustomerID) REFERENCES Customers (CustomerID) ON DELETE CASCADE ON UPDATE CASCADE; /* Gets deleted along with the customer */
ALTER TABLE CustomerFavorites ADD CONSTRAINT FK_CustomerFavorites_ProductType FOREIGN KEY (ProductTypeID) REFERENCES ProductType (ProductTypeID) ON DELETE RESTRICT ON UPDATE CASCADE;

/* George Washington favorites */
INSERT INTO CustomerFavorites (CustomerID, ProductTypeID) SELECT 1, ProductTypeID FROM ProductType WHERE Name = 'Drinks';
INSERT INTO CustomerFavorites (CustomerID, ProductTypeID) SELECT 1, ProductTypeID FROM ProductType WHERE Name = 'Frozen Foods';
INSERT INTO CustomerFavorites (CustomerID, ProductTypeID) SELECT 1, ProductTypeID FROM ProductType WHERE Name = 'Tobacco';

/* Thomas Jefferson favorites */
INSERT INTO CustomerFavorites (CustomerID, ProductTypeID) SELECT 2, ProductTypeID FROM ProductType WHERE Name = 'Snacks';

/* John Adams has no favorites */

/* Abraham Lincoln favorites */
INSERT INTO CustomerFavorites (CustomerID, ProductTypeID) SELECT 4, ProductTypeID FROM ProductType WHERE Name = 'Frozen Foods';
INSERT INTO CustomerFavorites (CustomerID, ProductTypeID) SELECT 4, ProductTypeID FROM ProductType WHERE Name = 'School Supplies';

/* Teddy Roosevelt favorites */
INSERT INTO CustomerFavorites (CustomerID, ProductTypeID) SELECT 5, ProductTypeID FROM ProductType WHERE Name = 'Tobacco';
INSERT INTO CustomerFavorites (CustomerID, ProductTypeID) SELECT 5, ProductTypeID FROM ProductType WHERE Name = 'Pharmacy';
INSERT INTO CustomerFavorites (CustomerID, ProductTypeID) SELECT 5, ProductTypeID FROM ProductType WHERE Name = 'Snacks';
INSERT INTO CustomerFavorites (CustomerID, ProductTypeID) SELECT 5, ProductTypeID FROM ProductType WHERE Name = 'Misc';
INSERT INTO CustomerFavorites (CustomerID, ProductTypeID) SELECT 5, ProductTypeID FROM ProductType WHERE Name = 'Ice Cream';
INSERT INTO CustomerFavorites (CustomerID, ProductTypeID) SELECT 5, ProductTypeID FROM ProductType WHERE Name = 'Drinks';



CREATE TABLE VendorOrders(
	VendorOrderID INT UNSIGNED NOT NULL AUTO_INCREMENT,
	VendorID INT UNSIGNED NOT NULL,
	DateOrdered  TIMESTAMP NOT NULL,
	DateDelivered  TIMESTAMP,
	PRIMARY KEY (VendorOrderID)
);

ALTER TABLE VendorOrders ADD CONSTRAINT FK_VendorOrders_Vendor FOREIGN KEY (VendorID) REFERENCES Vendors (VendorID) ON DELETE RESTRICT ON UPDATE CASCADE;


CREATE TABLE VendorOrderItems(
	VendorOrderID INT UNSIGNED NOT NULL,
	ItemID INT UNSIGNED NOT NULL,
	QuantityOrdered INT UNSIGNED NOT NULL,
	UnitCost DECIMAL(10,2) NOT NULL,
	/* Unit cost exists in VendorItems, but it's also here because pricing will vary over 
	 * time and the pricing change in the VendorItems table shouldn't impact previous 
	 * purchases. 
	 */
	PRIMARY KEY (VendorOrderID, ItemID)
);

ALTER TABLE VendorOrderItems ADD CONSTRAINT FK_VendorOrdersItems_VendorOrders FOREIGN KEY (VendorOrderID) REFERENCES VendorOrders (VendorOrderID) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE VendorOrderItems ADD CONSTRAINT FK_VendorOrdersItems_Inventory FOREIGN KEY (ItemID) REFERENCES Inventory (ItemID) ON DELETE RESTRICT ON UPDATE CASCADE;




# --- !Downs

drop table if exists AdminUsers;
drop table if exists ProductType;
drop table if exists Inventory;
drop table if exists Vendors;
drop table if exists VendorItems;
drop table if exists Customers;
drop table if exists PromotionDevices;
drop table if exists CustomerOrderItems;
drop table if exists CustomerFavorites;
drop table if exists VendorOrders;
drop table if exists VendorOrderItems;

