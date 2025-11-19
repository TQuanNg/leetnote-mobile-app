package com.example.leetnote.ui.screens.learning

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import javax.inject.Inject


data class PatternResponse(
    val patterns: List<PatternItem>
)

data class PatternItem(
    val id: Int,
    val name: String,
    val concept: String,
    val whenToUse: List<String>?,
    val approach: List<String>?,
    val complexity: Complexity,
    val examples: List<String>?,
    val tips: List<String>?
)

data class Complexity(
    val time: String,
    val space: String
)

@HiltViewModel
class LearningResViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _patterns = MutableStateFlow<List<PatternItem>>(emptyList())
    val patterns: StateFlow<List<PatternItem>> = _patterns

    init {
        loadPatterns()
    }

    private fun loadPatterns() {
        viewModelScope.launch {
            val inputStream = context.assets.open("patterns.json")
            val reader = InputStreamReader(inputStream)
            val response = Gson().fromJson(reader, PatternResponse::class.java)
            _patterns.value = response.patterns
            reader.close()
        }
    }
}