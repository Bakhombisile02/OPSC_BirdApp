package com.example.opsc_birdapp

class BirdsData (
    var location: String? = null,
    var name: String? = null,
    var species: String? = null,
    var user_id: String? = null
){
    // No-argument constructor required for Firebase serialization
    constructor() : this(null, null, null, null)
}
//----------------------------------------End of File ------------------------------------------------------//

