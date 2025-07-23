import SwiftUI
import shared

@main
struct iOSApp: App {
    let appContainer: AppContainer

    init() {
        // Initialize Room database for iOS
        let database = DatabaseBuilderKt.getRoomDatabase(
            builder: DatabaseBuilder_iosKt.getDatabaseBuilder()
        )
        self.appContainer = AppContainer(database: database)
    }

    var body: some Scene {
        WindowGroup {
            ContentView(appContainer: appContainer)
        }
    }
}