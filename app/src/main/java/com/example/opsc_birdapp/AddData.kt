package com.example.opsc_birdapp

class AddData (

        var fullName: String? = null,
        var email: String? = null,


        ) {
        // No-argument constructor required for Firebase serialization
        constructor() : this(null, null)
    }
