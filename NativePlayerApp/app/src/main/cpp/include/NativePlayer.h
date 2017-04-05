//
// Created by MrLukashem on 19.03.2017.
//

#ifndef NATIVEPLAYERAPP_NATIVEPLAYER_H
#define NATIVEPLAYERAPP_NATIVEPLAYER_H

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <string>
#include <vector>
#include <utility>

using SongsVec = std::vector<std::pair<std::string, int>>;
using SongPair = std::pair<std::string, int>;

class NativePlayer {
public:
    NativePlayer();

    virtual ~NativePlayer() {}

    virtual int load(const std::string& path);

    virtual int play(int dataID = 0);

    virtual void pause();

    virtual void seekTo(int ms);

    virtual int getCurrentPosition();

    virtual void stop();

    virtual void release();

    virtual SongsVec getSongs();

    enum NativePlayerMode {
        BASIC_MODE,
        CALLBACK_MODE,
        POOL_MODE
    };

    const int ERROR_CODE = -1;
private:
    SLObjectItf m_EngineObjectItf;
    SLEngineItf m_EngineItf;

    SLObjectItf m_PlayerItf;
    SLPlayItf m_PlayItf;

    int m_PlaybackMode;

    SongsVec m_SongsVec;

    int m_SongsCounter;

    virtual int initialize(int mode);

    virtual bool validatePathString(const std::string& path);
};


#endif //NATIVEPLAYERAPP_NATIVEPLAYER_H
