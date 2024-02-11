package com.example.appsearchtestproject.database

import androidx.appsearch.annotation.Document
import androidx.appsearch.app.AppSearchSchema

@Document
public data class Note(
    // Required field for a document class. All documents MUST have a namespace.
    @Document.Namespace
    val namespace: String,

    // Required field for a document class. All documents MUST have an Id.
    @Document.Id
    val id: String,

    // Optional field for a document class, used to index a note's text for this
    // document class.
    @Document.StringProperty(indexingType = AppSearchSchema.StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val text: String
)
