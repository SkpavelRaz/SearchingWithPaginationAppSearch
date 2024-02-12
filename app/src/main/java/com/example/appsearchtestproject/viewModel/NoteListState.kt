package com.example.appsearchtestproject.viewModel

import com.example.appsearchtestproject.database.Note

data class NoteListState(
    val note:List<Note> = emptyList(),
    val searchQuery:String=""
)
