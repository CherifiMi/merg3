package com.example.merg3

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.MimeTypes.AUDIO_AAC
import androidx.media3.transformer.*
import com.coremedia.iso.boxes.Container
import com.googlecode.mp4parser.authoring.Movie
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator
import java.io.File
import java.io.FileOutputStream
import java.nio.channels.FileChannel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        merge(
            videoPath = File(this.cacheDir, "fb_20230819_053037.mp4").path,
            audioPath = File(this.cacheDir, "fb_20230819_053046.mp3").path,
            context = this
        )

    }
}

private fun merge(
    videoPath: String,
    audioPath: String,
    context: Context,
) {
    //converted file name
    val cvn = "converted_${File(videoPath).name}.mp4"

    val output = File(context.cacheDir, "output.mp4")

    val transformerListener: Transformer.Listener =
        object : Transformer.Listener {
            override fun onCompleted(composition: Composition, result: ExportResult) {

                //merging video and audio
                val video: Movie = MovieCreator.build(File(context.cacheDir, cvn).path)
                val audio: Movie = MovieCreator.build(audioPath)

                video.addTrack(audio.getTracks().get(0))

                val out: Container = DefaultMp4Builder().build(video)

                //Write the output to a new file
                val fc: FileChannel =
                    FileOutputStream(output).getChannel()
                out.writeContainer(fc)
                fc.close()
            }

            override fun onError(
                composition: Composition, result: ExportResult,
                exception: ExportException
            ) {
                Log.d("LOGTAG", "FALED")
            }
        }

    //converting changing the input video codec
    val inputMediaItem = MediaItem.fromUri(Uri.fromFile(File(videoPath)))
    val editedMediaItem = EditedMediaItem.Builder(inputMediaItem).build()
    val transformer = Transformer.Builder(context)
        .setTransformationRequest(
            TransformationRequest.Builder().setVideoMimeType(MimeTypes.VIDEO_H264).build()
        )
        .addListener(transformerListener) // if changing the codec works -> merge it with the audio
        .build()

    transformer.start(editedMediaItem, File(context.cacheDir, cvn).path)

    output.path //the output file path
}





