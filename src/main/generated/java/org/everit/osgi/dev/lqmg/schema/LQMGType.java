/**
 * This file is part of Everit - Liquibase-QueryDSL Model Generator.
 *
 * Everit - Liquibase-QueryDSL Model Generator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Liquibase-QueryDSL Model Generator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Liquibase-QueryDSL Model Generator.  If not, see <http://www.gnu.org/licenses/>.
 */
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.17 at 09:59:59 PM CET 
//


package org.everit.osgi.dev.lqmg.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LQMGType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LQMGType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="entities" type="{http://everit.org/lqmg}LQMGEntitiesType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="defaultSchemaName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="defaultPackageName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="putSchemaIntoClass" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="defaultPrefix" type="{http://www.w3.org/2001/XMLSchema}string" default="Q" />
 *       &lt;attribute name="defaultSuffix" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LQMGType", propOrder = {
    "entities"
})
public class LQMGType {

    @XmlElement(required = true)
    protected LQMGEntitiesType entities;
    @XmlAttribute(name = "defaultSchemaName")
    protected String defaultSchemaName;
    @XmlAttribute(name = "defaultPackageName")
    protected String defaultPackageName;
    @XmlAttribute(name = "putSchemaIntoClass")
    protected Boolean putSchemaIntoClass;
    @XmlAttribute(name = "defaultPrefix")
    protected String defaultPrefix;
    @XmlAttribute(name = "defaultSuffix")
    protected String defaultSuffix;

    /**
     * Gets the value of the entities property.
     * 
     * @return
     *     possible object is
     *     {@link LQMGEntitiesType }
     *     
     */
    public LQMGEntitiesType getEntities() {
        return entities;
    }

    /**
     * Sets the value of the entities property.
     * 
     * @param value
     *     allowed object is
     *     {@link LQMGEntitiesType }
     *     
     */
    public void setEntities(LQMGEntitiesType value) {
        this.entities = value;
    }

    /**
     * Gets the value of the defaultSchemaName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultSchemaName() {
        return defaultSchemaName;
    }

    /**
     * Sets the value of the defaultSchemaName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultSchemaName(String value) {
        this.defaultSchemaName = value;
    }

    /**
     * Gets the value of the defaultPackageName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultPackageName() {
        return defaultPackageName;
    }

    /**
     * Sets the value of the defaultPackageName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultPackageName(String value) {
        this.defaultPackageName = value;
    }

    /**
     * Gets the value of the putSchemaIntoClass property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPutSchemaIntoClass() {
        if (putSchemaIntoClass == null) {
            return false;
        } else {
            return putSchemaIntoClass;
        }
    }

    /**
     * Sets the value of the putSchemaIntoClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPutSchemaIntoClass(Boolean value) {
        this.putSchemaIntoClass = value;
    }

    /**
     * Gets the value of the defaultPrefix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultPrefix() {
        if (defaultPrefix == null) {
            return "Q";
        } else {
            return defaultPrefix;
        }
    }

    /**
     * Sets the value of the defaultPrefix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultPrefix(String value) {
        this.defaultPrefix = value;
    }

    /**
     * Gets the value of the defaultSuffix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultSuffix() {
        return defaultSuffix;
    }

    /**
     * Sets the value of the defaultSuffix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultSuffix(String value) {
        this.defaultSuffix = value;
    }

}
