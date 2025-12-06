presentation: pres_build game pres_update

pres_build:
	rm -rf ../presention_landing/javadoc
	rm -rf ../presention_landing/*.zip
	./gradlew javadoc
	mv ./core/build/docs/javadoc ../presention_landing

pres_update: export FILE = $(shell find ../presention_landing/ -type f -regex '^.*.zip$$' | head -n 1 | sed 's/.*\/\(.*\)/\1/')
pres_update:
	sed -i 's/^\( *<a href="\).*\.zip\(".*ownload.*\)$$/\1${FILE}\2/' ../presention_landing/index.html
	cd ../presention_landing; zip -r presentation.zip ./*

game: game_build game_zip

game_build:
	rm -rf ./lwjgl3/build/libs/*
	./gradlew lwjgl3:dist

game_zip: export FILENAME = $(shell find ./lwjgl3/build/libs/ -type f -regex '^.*.jar$$' | head -n 1 | sed 's/.*\/\(.*\)\.jar/\1/')
game_zip:
	mkdir -p ./lwjgl3/build/tmp/game
	cp ./lwjgl3/build/libs/$$FILENAME.jar ./lwjgl3/build/tmp/game
	cp -r ./assets/objects ./lwjgl3/build/tmp/game
	cp -r ./assets/textures ./lwjgl3/build/tmp/game
	printf "To run the game, run the following your terminal:\n\`\`\`\njava -jar ${FILENAME}.jar\n\`\`\`\nRequires a JRE compatible with Java 21." >./lwjgl3/build/tmp/game/README.md
	cd ./lwjgl3/build/tmp/game; zip -r $$FILENAME.zip ./*
	mv ./lwjgl3/build/tmp/game/$$FILENAME.zip ../presention_landing
	rm -rf ./lwjgl3/build/tmp/game
