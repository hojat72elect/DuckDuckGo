

package com.duckduckgo.networkprotection.impl.settings.geoswitching

import com.duckduckgo.networkprotection.impl.settings.geoswitching.NetpEgressServersProvider.PreferredLocation
import com.duckduckgo.networkprotection.impl.settings.geoswitching.NetpEgressServersProvider.ServerLocation

class FakeNetpEgressServersProvider : NetpEgressServersProvider {
    override suspend fun updateServerLocationsAndReturnPreferred(): PreferredLocation? {
        TODO("Not yet implemented")
    }

    override suspend fun getServerLocations(): List<ServerLocation> {
        return listOf(
            ServerLocation(
                countryCode = "us",
                countryName = "United States",
                cities = listOf("El Segundo", "Chicago", "Atlanta", "Newark"),
            ),
            ServerLocation(
                countryCode = "gb",
                countryName = "UK",
                cities = emptyList(),
            ),
        )
    }
}
