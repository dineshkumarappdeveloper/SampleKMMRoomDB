package ai.bnm.myapplication

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform