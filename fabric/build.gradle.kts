import net.fabricmc.loom.api.fabricapi.FabricApiExtension

plugins {
    alias(libs.plugins.multimod)
}

multimod.modPublishing {
    modrinth {
        // Fabric API
        requires {
            slug = "P7dR8mSH"
        }
    }
}

multimod.fabric(project(":common"))
