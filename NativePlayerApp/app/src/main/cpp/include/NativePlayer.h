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
#include <memory>

using SongsVec = std::vector<std::pair<std::string, int>>;
using SongPair = std::pair<std::string, int>;

class NativePlayer {
public:
    NativePlayer();

    virtual ~NativePlayer();

    virtual int load(const std::string& path);

    virtual int play(int dataID = 0);

    virtual void pause();

    virtual void prepare();

    virtual void seekTo(int ms);

    virtual int getCurrentPosition();

    virtual void stop();

    virtual void release();

    virtual void setBufferSource(short *, int, int, int, int, int);

    virtual SongsVec getSongs();

    enum NativePlayerMode {
        BASIC_MODE,
        CALLBACK_MODE,
        POOL_MODE,
    };

    enum SourceMode {
    	BUFFER_SOURCE,
    	DATA_SOURCE,
    };

    const int ERROR_CODE = -1;

    void emptyBufferCallback();

private:
    SLObjectItf m_EngineObjectItf;
    SLEngineItf m_EngineItf;

    SLObjectItf m_PlayerItf;
    SLPlayItf  m_playbackItf;
    SLAndroidSimpleBufferQueueItf m_simpleAndroidBufferQueue;

    int m_PlaybackMode;

    SongsVec m_SongsVec;

    int m_SongsCounter;

    int m_sourceMode = DATA_SOURCE;

    // Playback parameters
    int m_bufferSize; // prefered buffer size
    int m_inputBufferSize; // input buffer size
    int m_sampleRate; // prefered sample rate
    // end playback parameters
    SLDataSource m_dataSource;
    SLDataSink m_sink;

    SLboolean m_required[3];
    SLInterfaceID m_idsArray[3];

    short* m_samplesBufferShort = nullptr;

    virtual int initialize(int mode);

    virtual bool validatePathString(const std::string& path);
};

#endif //NATIVEPLAYERAPP_NATIVEPLAYER_H
