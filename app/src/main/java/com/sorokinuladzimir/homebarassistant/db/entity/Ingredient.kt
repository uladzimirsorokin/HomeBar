package com.sorokinuladzimir.homebarassistant.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["id"], unique = true)])
class Ingredient {

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    var name: String? = null

    var description: String? = null

    var image: String? = null

    var notes: String? = null

    override fun toString(): String =
        "Ingredient{id=$id, name='$name', description='$description', image='$image', notes='$notes'}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Ingredient
        return id == that.id &&
            name == that.name &&
            description == that.description &&
            image == that.image &&
            notes == that.notes
    }

    override fun hashCode(): Int = listOf(id, name, description, image, notes).hashCode()
}
