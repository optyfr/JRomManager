package jrm.io.torrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jrm.io.torrent.bencoding.types.BByteString;
import jrm.io.torrent.bencoding.types.BDictionary;
import jrm.io.torrent.bencoding.types.BInt;
import jrm.io.torrent.bencoding.types.BList;

class TorrentParserTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should throw TorrentException when bencoded data is invalid")
    void parseTorrent_InvalidBencoding_ThrowsException() {
        byte[] invalidData = "not a bencoded dictionary".getBytes();
        
        assertThatThrownBy(() -> TorrentParser.parseTorrent(invalidData))
            .isInstanceOf(TorrentException.class);
    }

    @Test
    @DisplayName("Should throw TorrentException when info dictionary is missing")
    void parseTorrent_MissingInfoKey_ThrowsException() {
        // Construct a minimal bencoded dictionary without "info"
        // d[announce]...e
        BDictionary dict = new BDictionary();
        dict.add(new BByteString("announce"), new BByteString("http://tracker.com"));
        
        byte[] data = dict.bencode();
        
        assertThatThrownBy(() -> TorrentParser.parseTorrent(data))
            .isInstanceOf(TorrentException.class)
            .hasMessageContaining("Torrent has no info dictionary");
    }

    @Test
    @DisplayName("Should parse a valid single file torrent successfully")
    void parseTorrent_ValidSingleFile_ParsesCorrectly() throws TorrentException {
        BDictionary root = new BDictionary();
        BDictionary info = new BDictionary();
        
        info.add(new BByteString("name"), new BByteString("test.txt"));
        info.add(new BByteString("piece length"), new BInt(16384L));
        info.add(new BByteString("pieces"), new BByteString(new byte[20])); // 1 piece
        info.add(new BByteString("length"), new BInt(1000L));
        
        root.add(new BByteString("info"), info);
        root.add(new BByteString("announce"), new BByteString("http://announce.com"));
        
        byte[] data = root.bencode();
        Torrent result = TorrentParser.parseTorrent(data);
        
        assertThat(result.getName()).isEqualTo("test.txt");
        assertThat(result.getPieceLength()).isEqualTo(16384);
        assertThat(result.getTotalSize()).isEqualTo(1000);
        assertThat(result.getAnnounce()).isEqualTo("http://announce.com");
        assertThat(result.isSingleFileTorrent()).isTrue();
        assertThat(result.getInfoHash()).isNotNull();
    }

    @Test
    @DisplayName("Should parse a valid multi-file torrent successfully")
    void parseTorrent_ValidMultiFile_ParsesCorrectly() throws TorrentException {
        BDictionary root = new BDictionary();
        BDictionary info = new BDictionary();
        
        info.add(new BByteString("name"), new BByteString("folder"));
        info.add(new BByteString("piece length"), new BInt(16384L));
        info.add(new BByteString("pieces"), new BByteString(new byte[20]));
        
        BList files = new BList();
        BDictionary file1 = new BDictionary();
        file1.add(new BByteString("length"), new BInt(500L));
        BList path1 = new BList();
        path1.add(new BByteString("file1.txt"));
        file1.add(new BByteString("path"), path1);
        
        BDictionary file2 = new BDictionary();
        file2.add(new BByteString("length"), new BInt(700L));
        BList path2 = new BList();
        path2.add(new BByteString("file2.txt"));
        file2.add(new BByteString("path"), path2);
        
        files.add(file1);
        files.add(file2);
        
        info.add(new BByteString("files"), files);
        root.add(new BByteString("info"), info);
        
        byte[] data = root.bencode();
        Torrent result = TorrentParser.parseTorrent(data);
        
        assertThat(result.isSingleFileTorrent()).isFalse();
        assertThat(result.getTotalSize()).isEqualTo(1200);
    }

    @ParameterizedTest
    @ValueSource(strings = {"comment", "created by"})
    @DisplayName("Should parse optional fields when present")
    void parseTorrent_OptionalFields_ParsesCorrectly(String fieldName) throws TorrentException {
        BDictionary root = new BDictionary();
        BDictionary info = new BDictionary();
        info.add(new BByteString("name"), new BByteString("test"));
        info.add(new BByteString("piece length"), new BInt(16384L));
        info.add(new BByteString("pieces"), new BByteString(new byte[20]));
        info.add(new BByteString("length"), new BInt(100L));
        root.add(new BByteString("info"), info);
        
        String value = "test-value-" + fieldName;
        root.add(new BByteString(fieldName), new BByteString(value));
        
        Torrent result = TorrentParser.parseTorrent(root.bencode());
        
        if ("comment".equals(fieldName)) {
            assertThat(result.getComment()).isEqualTo(value);
        } else {
            assertThat(result.getCreatedBy()).isEqualTo(value);
        }
    }

    @Test
    @DisplayName("Should parse torrent from file path")
    void parseTorrent_FromFilePath_ParsesCorrectly() throws IOException, TorrentException {
        BDictionary root = new BDictionary();
        BDictionary info = new BDictionary();
        info.add(new BByteString("name"), new BByteString("file-test"));
        info.add(new BByteString("piece length"), new BInt(16384L));
        info.add(new BByteString("pieces"), new BByteString(new byte[20]));
        info.add(new BByteString("length"), new BInt(100L));
        root.add(new BByteString("info"), info);
        
        Path torrentFile = tempDir.resolve("test.torrent");
        Files.write(torrentFile, root.bencode());
        
        Torrent result = TorrentParser.parseTorrent(torrentFile.toString());
        
        assertThat(result.getName()).isEqualTo("file-test");
    }

    @Test
    @DisplayName("Should parse torrent from File object")
    void parseTorrent_FromFileObject_ParsesCorrectly() throws IOException, TorrentException {
        BDictionary root = new BDictionary();
        BDictionary info = new BDictionary();
        info.add(new BByteString("name"), new BByteString("file-obj-test"));
        info.add(new BByteString("piece length"), new BInt(32768L));
        info.add(new BByteString("pieces"), new BByteString(new byte[40])); // 2 pieces
        info.add(new BByteString("length"), new BInt(50000L));
        root.add(new BByteString("info"), info);
        
        Path torrentFile = tempDir.resolve("file-obj.torrent");
        Files.write(torrentFile, root.bencode());
        
        Torrent result = TorrentParser.parseTorrent(torrentFile.toFile());
        
        assertThat(result.getName()).isEqualTo("file-obj-test");
        assertThat(result.getPieceLength()).isEqualTo(32768L);
        assertThat(result.getPieces()).hasSize(2);
    }

    @Test
    @DisplayName("Should parse multiple pieces correctly")
    void parseTorrent_MultiplePieces_ParsesCorrectly() throws TorrentException {
        BDictionary root = new BDictionary();
        BDictionary info = new BDictionary();
        
        // 3 pieces = 60 bytes
        byte[] piecesData = new byte[60];
        for (int i = 0; i < 60; i++) {
            piecesData[i] = (byte) (i % 256);
        }
        
        info.add(new BByteString("name"), new BByteString("multi-piece"));
        info.add(new BByteString("piece length"), new BInt(16384L));
        info.add(new BByteString("pieces"), new BByteString(piecesData));
        info.add(new BByteString("length"), new BInt(49152L));
        root.add(new BByteString("info"), info);
        
        Torrent result = TorrentParser.parseTorrent(root.bencode());
        
        assertThat(result.getPieces()).hasSize(3);
        assertThat(result.getPieces()).allMatch(hash -> hash.length() == 40); // Each hash is 40 hex chars
    }

    @Test
    @DisplayName("Should parse creation date correctly")
    void parseTorrent_CreationDate_ParsesCorrectly() throws TorrentException {
        BDictionary root = new BDictionary();
        BDictionary info = new BDictionary();
        info.add(new BByteString("name"), new BByteString("dated-torrent"));
        info.add(new BByteString("piece length"), new BInt(16384L));
        info.add(new BByteString("pieces"), new BByteString(new byte[20]));
        info.add(new BByteString("length"), new BInt(1000L));
        root.add(new BByteString("info"), info);
        
        long creationTimestamp = 1609459200L; // 2021-01-01 00:00:00 UTC
        root.add(new BByteString("creation date"), new BInt(creationTimestamp));
        
        Torrent result = TorrentParser.parseTorrent(root.bencode());
        
        assertThat(result.getCreationDate()).isNotNull();
        assertThat(result.getCreationDate().getTime()).isEqualTo(creationTimestamp * 1000);
    }

    @Test
    @DisplayName("Should parse announce list correctly")
    void parseTorrent_AnnounceList_ParsesCorrectly() throws TorrentException {
        BDictionary root = new BDictionary();
        BDictionary info = new BDictionary();
        info.add(new BByteString("name"), new BByteString("announce-list-test"));
        info.add(new BByteString("piece length"), new BInt(16384L));
        info.add(new BByteString("pieces"), new BByteString(new byte[20]));
        info.add(new BByteString("length"), new BInt(1000L));
        root.add(new BByteString("info"), info);
        
        // Create announce-list with multiple tiers
        BList announceList = new BList();
        
        BList tier1 = new BList();
        tier1.add(new BByteString("http://tracker1.com/announce"));
        tier1.add(new BByteString("http://tracker2.com/announce"));
        
        BList tier2 = new BList();
        tier2.add(new BByteString("http://tracker3.com/announce"));
        
        announceList.add(tier1);
        announceList.add(tier2);
        
        root.add(new BByteString("announce-list"), announceList);
        
        Torrent result = TorrentParser.parseTorrent(root.bencode());
        
        assertThat(result.getAnnounceList()).hasSize(3);
        assertThat(result.getAnnounceList()).contains(
            "http://tracker1.com/announce",
            "http://tracker2.com/announce",
            "http://tracker3.com/announce"
        );
    }

    @Test
    @DisplayName("Should handle empty announce list")
    void parseTorrent_EmptyAnnounceList_ReturnsEmptyList() throws TorrentException {
        BDictionary root = new BDictionary();
        BDictionary info = new BDictionary();
        info.add(new BByteString("name"), new BByteString("empty-announce"));
        info.add(new BByteString("piece length"), new BInt(16384L));
        info.add(new BByteString("pieces"), new BByteString(new byte[20]));
        info.add(new BByteString("length"), new BInt(1000L));
        root.add(new BByteString("info"), info);
        
        BList emptyAnnounceList = new BList();
        root.add(new BByteString("announce-list"), emptyAnnounceList);
        
        Torrent result = TorrentParser.parseTorrent(root.bencode());
        
        assertThat(result.getAnnounceList()).isEmpty();
    }

    @Test
    @DisplayName("Should parse multi-file torrent with nested paths")
    void parseTorrent_MultiFileWithNestedPaths_ParsesCorrectly() throws TorrentException {
        BDictionary root = new BDictionary();
        BDictionary info = new BDictionary();
        
        info.add(new BByteString("name"), new BByteString("root-folder"));
        info.add(new BByteString("piece length"), new BInt(16384L));
        info.add(new BByteString("pieces"), new BByteString(new byte[20]));
        
        BList files = new BList();
        
        // File with nested path: subfolder/file1.txt
        BDictionary file1 = new BDictionary();
        file1.add(new BByteString("length"), new BInt(100L));
        BList path1 = new BList();
        path1.add(new BByteString("subfolder"));
        path1.add(new BByteString("file1.txt"));
        file1.add(new BByteString("path"), path1);
        
        // File with deeper nesting: a/b/c/file2.txt
        BDictionary file2 = new BDictionary();
        file2.add(new BByteString("length"), new BInt(200L));
        BList path2 = new BList();
        path2.add(new BByteString("a"));
        path2.add(new BByteString("b"));
        path2.add(new BByteString("c"));
        path2.add(new BByteString("file2.txt"));
        file2.add(new BByteString("path"), path2);
        
        files.add(file1);
        files.add(file2);
        
        info.add(new BByteString("files"), files);
        root.add(new BByteString("info"), info);
        
        Torrent result = TorrentParser.parseTorrent(root.bencode());
        
        assertThat(result.isSingleFileTorrent()).isFalse();
        assertThat(result.getFileList()).hasSize(2);
        assertThat(result.getTotalSize()).isEqualTo(300L);
        
        assertThat(result.getFileList().get(0).getFileDirs()).containsExactly("subfolder", "file1.txt");
        assertThat(result.getFileList().get(1).getFileDirs()).containsExactly("a", "b", "c", "file2.txt");
    }

    @Test
    @DisplayName("Should calculate correct info hash")
    void parseTorrent_InfoHash_CalculatesCorrectly() throws TorrentException {
        BDictionary root = new BDictionary();
        BDictionary info = new BDictionary();
        info.add(new BByteString("name"), new BByteString("hash-test"));
        info.add(new BByteString("piece length"), new BInt(16384L));
        info.add(new BByteString("pieces"), new BByteString(new byte[20]));
        info.add(new BByteString("length"), new BInt(1000L));
        root.add(new BByteString("info"), info);
        
        Torrent result = TorrentParser.parseTorrent(root.bencode());
        
        assertThat(result.getInfoHash()).isNotNull();
        assertThat(result.getInfoHash()).hasSize(40); // SHA-1 hash is 40 hex characters
        assertThat(result.getInfoHash()).matches("[0-9a-f]+"); // Should be lowercase hex
    }

    @Test
    @DisplayName("Should parse large file sizes correctly")
    void parseTorrent_LargeFileSizes_ParsesCorrectly() throws TorrentException {
        BDictionary root = new BDictionary();
        BDictionary info = new BDictionary();
        
        long largeSize = 10L * 1024 * 1024 * 1024; // 10 GB
        long pieceLength = 4L * 1024 * 1024; // 4 MB
        
        info.add(new BByteString("name"), new BByteString("large-file"));
        info.add(new BByteString("piece length"), new BInt(pieceLength));
        info.add(new BByteString("pieces"), new BByteString(new byte[20]));
        info.add(new BByteString("length"), new BInt(largeSize));
        root.add(new BByteString("info"), info);
        
        Torrent result = TorrentParser.parseTorrent(root.bencode());
        
        assertThat(result.getTotalSize()).isEqualTo(largeSize);
        assertThat(result.getPieceLength()).isEqualTo(pieceLength);
    }

    @Test
    @DisplayName("Should parse multi-file torrent with many files")
    void parseTorrent_ManyFiles_ParsesCorrectly() throws TorrentException {
        BDictionary root = new BDictionary();
        BDictionary info = new BDictionary();
        
        info.add(new BByteString("name"), new BByteString("many-files"));
        info.add(new BByteString("piece length"), new BInt(16384L));
        info.add(new BByteString("pieces"), new BByteString(new byte[20]));
        
        BList files = new BList();
        long totalSize = 0;
        
        for (int i = 0; i < 10; i++) {
            BDictionary file = new BDictionary();
            long size = (i + 1) * 100L;
            file.add(new BByteString("length"), new BInt(size));
            BList path = new BList();
            path.add(new BByteString("file" + i + ".txt"));
            file.add(new BByteString("path"), path);
            files.add(file);
            totalSize += size;
        }
        
        info.add(new BByteString("files"), files);
        root.add(new BByteString("info"), info);
        
        Torrent result = TorrentParser.parseTorrent(root.bencode());
        
        assertThat(result.getFileList()).hasSize(10);
        assertThat(result.getTotalSize()).isEqualTo(totalSize);
        assertThat(result.isSingleFileTorrent()).isFalse();
    }

    @Test
    @DisplayName("Should parse torrent with all optional fields")
    void parseTorrent_AllOptionalFields_ParsesCorrectly() throws TorrentException {
        BDictionary root = new BDictionary();
        BDictionary info = new BDictionary();
        info.add(new BByteString("name"), new BByteString("complete-torrent"));
        info.add(new BByteString("piece length"), new BInt(16384L));
        info.add(new BByteString("pieces"), new BByteString(new byte[20]));
        info.add(new BByteString("length"), new BInt(1000L));
        root.add(new BByteString("info"), info);
        
        root.add(new BByteString("announce"), new BByteString("http://main-tracker.com"));
        root.add(new BByteString("comment"), new BByteString("This is a test torrent"));
        root.add(new BByteString("created by"), new BByteString("Test Creator v1.0"));
        root.add(new BByteString("creation date"), new BInt(1609459200L));
        
        Torrent result = TorrentParser.parseTorrent(root.bencode());
        
        assertThat(result.getAnnounce()).isEqualTo("http://main-tracker.com");
        assertThat(result.getComment()).isEqualTo("This is a test torrent");
        assertThat(result.getCreatedBy()).isEqualTo("Test Creator v1.0");
        assertThat(result.getCreationDate()).isNotNull();
        assertThat(result.getName()).isEqualTo("complete-torrent");
        assertThat(result.getInfoHash()).isNotNull();
    }
}
