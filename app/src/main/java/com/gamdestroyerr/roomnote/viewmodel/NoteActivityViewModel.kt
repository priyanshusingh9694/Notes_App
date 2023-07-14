package com.gamdestroyerr.roomnote.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Todo
import com.gamdestroyerr.roomnote.model.Note
import com.gamdestroyerr.roomnote.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.logging.Handler

class NoteActivityViewModel(private val repositoryObject: NoteRepository) : ViewModel() {

    fun saveNote(newNote: Note) = viewModelScope.launch(Dispatchers.IO) {
        //repositoryObject.addNote(newNote)
        val item: Todo = Todo.builder()
            .title(newNote.title)
            .content(newNote.content)
            .date(newNote.date)
            .color(newNote.color)
            .imagePath(newNote.imagePath)
            .newid(newNote.id)
            .build()
        Amplify.DataStore.save(
            item,
            { success -> Log.i("Amplify", "Saved item: " + success.item().title) },
            { error -> Log.e("Amplify", "Could not save item to DataStore", error) }
        )
    }

    private var imagePath: String? = null

    fun saveImagePath(path: String?) {
        imagePath = path
    }

    fun setImagePath(): String? {
        if (imagePath != null)
            return imagePath
        return null
    }

    fun updateNote(existingNote: Note) = viewModelScope.launch(Dispatchers.IO) {
        //repositoryObject.updateNote(existingNote)
        Amplify.DataStore.query(
            Todo::class.java,
            { items ->
                while (items.hasNext()) {
                    val item = items.next()
                    if (existingNote.newId == item.id) {
                        Amplify.DataStore.delete(
                            item,
                            { success -> Log.i("Amplify", "Updated item: " + success.item().title) },
                            { error -> Log.e("Amplify", "Could not save item to DataStore", error) }
                        )
                        val item: Todo = Todo.builder()
                            .title(existingNote.title)
                            .content(existingNote.content)
                            .date(existingNote.date)
                            .color(existingNote.color)
                            .imagePath(existingNote.imagePath)
                            .newid(existingNote.id)
                            .build()
                        Amplify.DataStore.save(
                            item,
                            { success -> Log.i("Amplify", "Saved item: " + success.item().title) },
                            { error -> Log.e("Amplify", "Could not save item to DataStore", error) }
                        )
                    }
                }
            },
        ) { failure -> Log.e("Amplify", "Cannot Get Item", failure) }
    }

    fun deleteNote(existingNote: Note) = viewModelScope.launch(Dispatchers.IO) {
        Amplify.DataStore.query(Todo::class.java,{
            items ->
        while (items.hasNext()) {
            val item = items.next()
            if (existingNote.newId == item.id){
                Amplify.DataStore.delete(item,
                    { deleted -> Log.i("Amplify", "Deleted item.") },
                    { failure -> Log.e("Amplify", "Delete failed.", failure) }
                )
            }
        }
    },
        ) { failure -> Log.e("Amplify", "Cannot Get Item", failure) }
    }

    fun searchNote(query: String): LiveData<List<Note>> {
        return repositoryObject.searchNote(query)
    }

    fun getAllNotes(): LiveData<List<Note>> {
        var data = MutableLiveData<List<Note>>()
        var list = ArrayList<Note>()
        Amplify.DataStore.query(
            Todo::class.java,
            { items ->
                while (items.hasNext()) {
                    val item = items.next()
                    Log.i("Amplify", "Queried item: " + item.id)
                    list.add(Note(item.newid,item.title,item.content,item.date,item.color,item.imagePath,item.id))
                }
            },
            { failure -> Log.e("Amplify", "Could not query DataStore", failure) }
        )
        data.postValue(list)
        return data
    }

    override fun onCleared() {
        imagePath = null
        super.onCleared()
    }
}