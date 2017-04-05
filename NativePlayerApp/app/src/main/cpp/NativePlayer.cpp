//
// Created by MrLukashem on 19.03.2017.
//

#include "NativePlayer.h"
#include "include/common.h"

#define APPNAME "NativePlayer"

NativePlayer::NativePlayer() {
    initialize(BASIC_MODE);
}

int NativePlayer::initialize(int mode) {
    switch (mode) {
        case BASIC_MODE:
            break;
        case CALLBACK_MODE:
            break;
        case POOL_MODE:
            break;
        default:
            break;
    }

    auto res = slCreateEngine(&m_EngineObjectItf, 0, nullptr, 0, nullptr, nullptr);
    ALOGV("The engine has been created");

    res = (*m_EngineObjectItf)->GetInterface(m_EngineObjectItf, SL_IID_ENGINE, &m_EngineItf);

    return 0;
}

bool NativePlayer::validatePathString(const std::string &path) {
    if (path.empty()) {
        return ERROR_CODE;
    }

    return true;
}

int NativePlayer::load(const std::string &path) {
    if (not validatePathString(path)) {
        return ERROR_CODE;
    }

    m_SongsVec.push_back(std::move(SongPair{path, ++m_SongsCounter}));
    return m_SongsCounter;
}

void NativePlayer::pause() {

}

int NativePlayer::play(int dataID) {
    return 0;
}

SongsVec NativePlayer::getSongs() {
    return SongsVec{};
}

void NativePlayer::seekTo(int ms) {

}

int NativePlayer::getCurrentPosition() {
    return 0;
}

void NativePlayer::stop() {

}

void NativePlayer::release() {

}

















