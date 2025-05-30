import androidx.room.TypeConverter

class Converter { @TypeConverter
fun fromStringList(value: List<String>): String {
    return value.joinToString(separator = ",")
}

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return value.split(",")
    }

}