/*
 * Copyright (c) 2012 Orderly Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics.snowplow.hadoop.hive

// Specs2
import org.specs2.mutable.Specification

// Hive
import org.apache.hadoop.hive.serde2.SerDeException;

class DeserializeTest extends Specification {

  // Toggle if tests are failing and you want to inspect the struct contents
  val DEBUG = false;

  // -------------------------------------------------------------------------------------------------------------------
  // Invalid/header row checks
  // -------------------------------------------------------------------------------------------------------------------

  "An invalid or corrupted CloudFront row should throw an exception" >> {
    Seq("", "NOT VALID", "2012-05-21\t07:14:47\tFRA2\t3343\t83.4.209.35\tGET\td3t05xllj8hhgj.cloudfront.net") foreach { invalid =>
      "invalid row \"%s\" throws a SerDeException".format(invalid) >> {
        SnowPlowEventDeserializer.deserializeLine(invalid, DEBUG) must throwA[SerDeException](message = "Could not parse row: " + invalid)
      }
    }
  }

  "The header rows of a CloudFront log file should be skipped" >> {
    Seq("#Version: 1.0", "#Fields: date time x-edge-location sc-bytes c-ip cs-method cs(Host) cs-uri-stem sc-status cs(Referer) cs(User-Agent) cs-uri-query") foreach { header => 
      "header row \"%s\" is skipped (returns null)".format(header) >> {
        SnowPlowEventDeserializer.deserializeLine(header, DEBUG).asInstanceOf[SnowPlowEventStruct].dt must beNull
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Type checks
  // -------------------------------------------------------------------------------------------------------------------

  val types = "2012-05-21\t07:14:47\tFRA2\t3343\t83.4.209.35\tGET\td3t05xllj8hhgj.cloudfront.net\t/ice.png\t200\thttps://test.psybazaar.com/shop/checkout/\tMozilla/5.0%20(X11;%20Ubuntu;%20Linux%20x86_64;%20rv:11.0)%20Gecko/20100101%20Firefox/11.0\t&ev_ca=ecomm&ev_ac=checkout&ev_la=id_email&ev_pr=ERROR&r=236095&urlref=http%253A%252F%252Ftest.psybazaar.com%252F&_id=135f6b7536aff045&lang=en-US&visit=5&pdf=0&qt=1&realp=0&wma=1&dir=0&fla=1&java=1&gears=0&ag=0&res=1920x1080&cookie=1"

  "The CloudFront row \"%s\"".format(types) should {

    val event = SnowPlowEventDeserializer.deserializeLine(types, DEBUG)

    // Check main type
    "deserialize as a SnowPlowEventStruct" in {
      event must beAnInstanceOf[SnowPlowEventStruct]
    }

    val eventStruct = event.asInstanceOf[SnowPlowEventStruct]

    // Check all of the field types

    // Date/time
    "with a dt (Date) field which is a Hive STRING" in {
      eventStruct.dt must beAnInstanceOf[java.lang.String]
    }
    "with a tm (Time) field which is a Hive STRING" in {
      eventStruct.tm must beAnInstanceOf[java.lang.String]
    }

    // User and visit
    "with a user_ipaddress (User IP Address) field which is a Hive STRING" in {
      eventStruct.user_ipaddress must beAnInstanceOf[java.lang.String]
    }

    // Page
    "with a page_url (Page URL) field which is a Hive STRING" in {
      eventStruct.page_url must beAnInstanceOf[java.lang.String]
    }

    // Browser (from user-agent)
    "with a br_name (Browser Name) field which is a Hive STRING" in {
      eventStruct.br_name must beAnInstanceOf[java.lang.String]
    }
    "with a br_family (Browser Family) field which is a Hive STRING" in {
      eventStruct.br_family must beAnInstanceOf[java.lang.String]
    }
    "with a br_version (Browser Version) field which is a Hive STRING" in {
      eventStruct.br_version must beAnInstanceOf[java.lang.String]
    }
    "with a br_type (Browser Type) field which is a Hive STRING" in {
      eventStruct.br_type must beAnInstanceOf[java.lang.String]
    }
    "with a br_renderengine (Browser Rendering Engine) field which is a Hive STRING" in {
      eventStruct.br_renderengine must beAnInstanceOf[java.lang.String]
    }
    "with a os_name (OS Name) field which is a Hive STRING" in {
      eventStruct.os_name must beAnInstanceOf[java.lang.String]
    }
    "with a os_family (OS Family) field which is a Hive STRING" in {
      eventStruct.os_family must beAnInstanceOf[java.lang.String]
    }
    "with a os_manufacturer (OS Manufacturer) field which is a Hive STRING" in {
      eventStruct.os_manufacturer must beAnInstanceOf[java.lang.String]
    }
    "with a dvce_ismobile (Device Is Mobile?) field which is a Hive BOOLEAN" in {
      eventStruct.dvce_ismobile must beAnInstanceOf[java.lang.Boolean]
    }
    "with a dvce_type (Device Type) field which is a Hive STRING" in {
      eventStruct.dvce_type must beAnInstanceOf[java.lang.String]
    }
    // TODO
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Value checks
  // -------------------------------------------------------------------------------------------------------------------

  // Now let's check the specific values for another couple of lines
  // TODO let's parameterize this using the example in
  // TODO http://stackoverflow.com/questions/6805267/scalatest-or-specs2-with-multiple-test-cases

  // -------------------------------------------------------------------------------------------------------------------
  // Fallback checks
  // -------------------------------------------------------------------------------------------------------------------

  // Let's check that a "-" cs(Referer) is successfully replaced with the querystring url
  // TODO
}
