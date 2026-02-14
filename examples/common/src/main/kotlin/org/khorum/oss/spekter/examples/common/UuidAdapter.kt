package org.khorum.oss.spekter.examples.common

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.util.UUID

class UuidAdapter : XmlAdapter<String, UUID>() {
    override fun unmarshal(v: String): UUID = UUID.fromString(v)
    override fun marshal(v: UUID): String = v.toString()
}