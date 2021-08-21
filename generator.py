import os
import shutil

name = "Cool Elytra"
modid = "cool_elytra"
maven_group = "edu.jorbonism"
archives_base_name = "cool_elytra"
version = "0.1.0"
description = "This is an example description! Tell everyone what your mod is about!"
author = "Jorbonism"

main_init_class = ""
client_init_class = ""
server_init_class = ""

loom_version = "0.8-SNAPSHOT"
minecraft_version = "1.17.1"
yarn_mappings = "1.17.1+build.39"
loader_version = "0.11.6"
fabric_version = "0.37.2+1.17"

# https://fabricmc.net/versions.html


template_path = os.path.join(os.path.expanduser("~"), "Projects", "minecraft modding", "template")



home = os.path.join(os.path.dirname(__file__), archives_base_name)
shutil.copytree(template_path, home)
shutil.copyfile(__file__, os.path.join(home, "generator.py"))

dirparts = maven_group.split(".")
os.mkdir(os.path.join(home, "src", "main", "java", dirparts[0]))
os.mkdir(os.path.join(home, "src", "main", "java", dirparts[0], dirparts[1]))
os.mkdir(os.path.join(home, "src", "main", "java", dirparts[0], dirparts[1], archives_base_name))
os.mkdir(os.path.join(home, "src", "main", "java", dirparts[0], dirparts[1], archives_base_name, "mixin"))
os.mkdir(os.path.join(home, "src", "main", "resources", "assets", modid))

file1 = open(os.path.join(home, "build.gradle"), "w")
file1.write("plugins {\n	id 'fabric-loom' version '" + loom_version + "'\n	id 'maven-publish'\n}\n\nsourceCompatibility = JavaVersion.VERSION_16\ntargetCompatibility = JavaVersion.VERSION_16\n\narchivesBaseName = project.archives_base_name\nversion = project.mod_version\ngroup = project.maven_group\n\nrepositories {\n	\n}\n\ndependencies {\n	minecraft \"com.mojang:minecraft:${project.minecraft_version}\"\n	mappings \"net.fabricmc:yarn:${project.yarn_mappings}:v2\"\n	modImplementation \"net.fabricmc:fabric-loader:${project.loader_version}\"\n	modImplementation \"net.fabricmc.fabric-api:fabric-api:${project.fabric_version}\"\n}\n\nprocessResources {\n	inputs.property \"version\", project.version\n\n	filesMatching(\"fabric.mod.json\") {\n		expand \"version\": project.version\n	}\n}\n\ntasks.withType(JavaCompile).configureEach {\n	it.options.encoding = \"UTF-8\"\n	it.options.release = 16\n}\n\njava {\n	withSourcesJar()\n}\n\njar {\n	from(\"LICENSE\") {\n		rename { \"${it}_${project.archivesBaseName}\"}\n	}\n}\n\npublishing {\n	publications {\n		mavenJava(MavenPublication) {\n			artifact(remapJar) {\n				builtBy remapJar\n			}\n			artifact(sourcesJar) {\n				builtBy remapSourcesJar\n			}\n		}\n	}\n\n	repositories {\n		\n	}\n}\n")
file1.close()

file2 = open(os.path.join(home, "gradle.properties"), "w")
file2.write("org.gradle.jvmargs=-Xmx1G\n\nminecraft_version=" + minecraft_version + "\nyarn_mappings=" + yarn_mappings + "\nloader_version=" + loader_version + "\n\nmod_version = " + version + "\nmaven_group = " + maven_group + "\narchives_base_name = " + archives_base_name + "\n\nfabric_version=" + fabric_version + "\n")
file2.close()


init_class_list = []
if (len(main_init_class) > 0):
    init_class_list.append("\"main\": [\n		\"edu.jorbonism." + archives_base_name + "." + main_init_class + "\"\n		]")
if (len(client_init_class) > 0):
    init_class_list.append("\"client\": [\n		\"edu.jorbonism." + archives_base_name + "." + client_init_class + "\"\n		]")
if (len(server_init_class) > 0):
    init_class_list.append("\"server\": [\n		\"edu.jorbonism." + archives_base_name + "." + server_init_class + "\"\n		]")

entrypoints = ""
if (len(init_class_list) > 0):
    entrypoints = "\n	\"entrypoints\": {\n		" + (",\n		".join(init_class_list)) + "\n	},"

file3 = open(os.path.join(home, "src", "main", "resources", "fabric.mod.json"), "w")
file3.write("{\n	\"schemaVersion\": 1,\n	\"id\": \"" + modid + "\",\n	\"version\": \"${version}\",\n	\n	\"name\": \"" + name + "\",\n	\"description\": \"" + description + "\",\n	\"authors\": [\n		\"" + author + "\"\n	],\n	\"contact\": {\n		\"homepage\": \"https://fabricmc.net/\",\n		\"sources\": \"\"\n	},\n	\n	\"license\": \"CC0-1.0\",\n	\"icon\": \"assets/" + modid + "/icon.png\",\n	\n	\"environment\": \"*\"," + entrypoints + "\n	\"mixins\": [\n		\"" + modid + ".mixins.json\"\n	],\n	\n	\"depends\": {\n		\"fabricloader\": \">=" + loader_version + "\",\n		\"fabric\": \"*\",\n		\"minecraft\": \"" + minecraft_version[:4] + ".x\",\n		\"java\": \">=16\"\n	},\n	\"suggests\": {\n		\"another-mod\": \"*\"\n	}\n}\n")
file3.close()

file4 = open(os.path.join(home, "src", "main", "resources", modid + ".mixins.json"), "w")
file4.write("{\n	\"required\": true,\n	\"minVersion\": \"0.8\",\n	\"package\": \"" + maven_group + "." + archives_base_name + ".mixin\",\n	\"compatibilityLevel\": \"JAVA_16\",\n	\"mixins\": [\n		\"ExampleMixin\"\n	],\n	\"client\": [\n		\n	],\n	\"server\": [\n\n	],\n	\"injectors\": {\n		\"defaultRequire\": 1\n	}\n}\n")
file4.close()


if (len(main_init_class) > 0):
    file5a = open(os.path.join(home, "src", "main", "java", dirparts[0], dirparts[1], archives_base_name, main_init_class + ".java"), "w")
    file5a.write("package " + maven_group + "." + archives_base_name + ";\n\nimport net.fabricmc.api.ModInitializer;\n\npublic class " + main_init_class + " implements ModInitializer {\n	@Override\n	public void onInitialize() {\n		// This code runs as soon as Minecraft is in a mod-load-ready state.\n		// However, some things (like resources) may still be uninitialized.\n		// Proceed with mild caution.\n\n		System.out.println(\"Hello Fabric world!\");\n	}\n}\n")
    file5a.close()

if (len(client_init_class) > 0 and main_init_class != client_init_class):
    file5b = open(os.path.join(home, "src", "main", "java", dirparts[0], dirparts[1], archives_base_name, client_init_class + ".java"), "w")
    file5b.write("// no generator code for this yet")
    file5b.close()

if (len(server_init_class) > 0 and main_init_class != server_init_class and client_init_class != server_init_class):
    file5c = open(os.path.join(home, "src", "main", "java", dirparts[0], dirparts[1], archives_base_name, server_init_class + ".java"), "w")
    file5c.write("// no generator code for this yet")
    file5c.close()



file6 = open(os.path.join(home, "src", "main", "java", dirparts[0], dirparts[1], archives_base_name, "mixin", "ExampleMixin.java"), "w")
file6.write("package " + maven_group + "." + archives_base_name + ".mixin;\n\nimport net.minecraft.client.gui.screen.TitleScreen;\nimport org.spongepowered.asm.mixin.Mixin;\nimport org.spongepowered.asm.mixin.injection.At;\nimport org.spongepowered.asm.mixin.injection.Inject;\nimport org.spongepowered.asm.mixin.injection.callback.CallbackInfo;\n\n@Mixin(TitleScreen.class)\npublic class ExampleMixin {\n	@Inject(at = @At(\"HEAD\"), method = \"init()V\")\n	private void init(CallbackInfo info) {\n		System.out.println(\"This line is printed by an example mod mixin!\");\n	}\n}\n")
file6.close()


