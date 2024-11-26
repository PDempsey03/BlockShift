@echo Attempting to start Firebase emulator

:: If true, run full emulator which allows access to ui view, otherwise run bare minimum for tests
@echo Off
if /I "%1" == "true" (
    :: Change to the working directory
    cd /d "%~dp0"

    :: Launch the emulator in a separate window
    firebase emulators:start

    @echo Loading full emulator
) else (
    :: Launch emulator in a separate window
    firebase emulators:start --only firestore

    @echo Loading minimal emulator
)
