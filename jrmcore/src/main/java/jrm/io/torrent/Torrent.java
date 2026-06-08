/* Copyright (C) 2015  Christophe De Troyer
 * Copyright (C) 2018  Optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.io.torrent;

import java.util.Date;
import java.util.List;

/**
 * Represents a parsed BitTorrent metainfo file (.torrent). Holds all metadata
 * regarding the trackers, files, piece hashes, sizes, creation details, and the
 * info-hash of the torrent.
 * 
 * @author Christophe De Troyer
 * @author Optyfr
 */
public class Torrent {
    /**
     * The announce URL of the primary tracker.
     */
    private String announce;

    /**
     * The name of the file or directory suggested for saving.
     */
    private String name;

    /**
     * The size in bytes of each piece of the torrent.
     */
    private Long pieceLength;

    /**
     * The raw concatenated byte array of 20-byte SHA-1 hashes for all pieces.
     */
    private byte[] piecesBlob;

    /**
     * The list of 20-byte SHA-1 piece hashes represented as hexadecimal strings.
     */
    private List<String> pieces;

    /**
     * Flag indicating whether the torrent represents a single file or a multi-file
     * layout.
     */
    private boolean singleFileTorrent;

    /**
     * The total size in bytes of all files in the torrent.
     */
    private Long totalSize;

    /**
     * The list of individual files included in a multi-file torrent, or null if
     * single-file.
     */
    private List<TorrentFile> fileList;

    /**
     * An optional comment description of the torrent.
     */
    private String comment;

    /**
     * An optional string identifying the creator or program that produced the
     * torrent.
     */
    private String createdBy;

    /**
     * An optional date when the torrent metainfo was created.
     */
    private Date creationDate;

    /**
     * Optional secondary tier list of tracker announce URLs (announce-list).
     */
    private List<String> announceList;

    /**
     * The hexadecimal SHA-1 info-hash identifying the torrent contents.
     */
    private String infoHash;

    /**
     * Constructs a new Torrent object with default values. The fields of the Torrent object are initialized to their default values (null for objects and false for boolean).
     */
    public Torrent() {
        // Default constructor
    }
    
    // Getters and setters

    /**
     * Gets the list of individual files in a multi-file torrent.
     * 
     * @return the list of {@link TorrentFile} objects
     */
    public List<TorrentFile> getFileList() {
        return fileList;
    }

    /**
     * Sets the list of individual files in a multi-file torrent.
     * 
     * @param fileList the list of {@link TorrentFile} objects to set
     */
    public void setFileList(List<TorrentFile> fileList) {
        this.fileList = fileList;
    }

    /**
     * Gets the primary tracker's announce URL.
     * 
     * @return the primary tracker announce URL
     */
    public String getAnnounce() {
        return announce;
    }

    /**
     * Sets the primary tracker's announce URL.
     * 
     * @param announce the primary tracker announce URL to set
     */
    public void setAnnounce(String announce) {
        this.announce = announce;
    }

    /**
     * Gets the suggested name of the torrent content.
     * 
     * @return the suggested content name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the suggested name of the torrent content.
     * 
     * @param name the suggested content name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the length of a single piece in bytes.
     * 
     * @return the size of each piece
     */
    public Long getPieceLength() {
        return pieceLength;
    }

    /**
     * Sets the length of a single piece in bytes.
     * 
     * @param pieceLength the size of each piece to set
     */
    public void setPieceLength(Long pieceLength) {
        this.pieceLength = pieceLength;
    }

    /**
     * Gets the raw concatenated pieces byte array of 20-byte hashes.
     * 
     * @return the raw pieces byte array
     */
    public byte[] getPiecesBlob() {
        return piecesBlob;
    }

    /**
     * Sets the raw concatenated pieces byte array of 20-byte hashes.
     * 
     * @param piecesBlob the raw pieces byte array to set
     */
    public void setPiecesBlob(byte[] piecesBlob) {
        this.piecesBlob = piecesBlob;
    }

    /**
     * Gets the piece SHA-1 hashes represented as a list of hex strings.
     * 
     * @return the list of piece hashes
     */
    public List<String> getPieces() {
        return pieces;
    }

    /**
     * Sets the piece SHA-1 hashes represented as a list of hex strings.
     * 
     * @param pieces the list of piece hashes to set
     */
    public void setPieces(List<String> pieces) {
        this.pieces = pieces;
    }

    /**
     * Checks if this is a single file torrent.
     * 
     * @return {@code true} if single-file, otherwise {@code false}
     */
    public boolean isSingleFileTorrent() {
        return singleFileTorrent;
    }

    /**
     * Sets whether this is a single file torrent.
     * 
     * @param singleFileTorrent {@code true} if single-file, otherwise {@code false}
     */
    public void setSingleFileTorrent(boolean singleFileTorrent) {
        this.singleFileTorrent = singleFileTorrent;
    }

    /**
     * Gets the total size in bytes of all files in this torrent.
     * 
     * @return the total torrent size
     */
    public Long getTotalSize() {
        return totalSize;
    }

    /**
     * Sets the total size in bytes of all files in this torrent.
     * 
     * @param totalSize the total torrent size to set
     */
    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    /**
     * Gets the optional torrent comment.
     * 
     * @return the comment description, or null if none
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the optional torrent comment.
     * 
     * @param comment the comment description to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Gets the optional creator signature.
     * 
     * @return the creator's program name/signature, or null
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the optional creator signature.
     * 
     * @param createdBy the creator's program name/signature to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Gets the optional creation date of the torrent.
     * 
     * @return the creation {@link Date}, or null
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the optional creation date of the torrent.
     * 
     * @param creationDate the creation {@link Date} to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Gets the secondary trackers' announce-list URLs.
     * 
     * @return the announce-list containing list of trackers
     */
    public List<String> getAnnounceList() {
        return announceList;
    }

    /**
     * Sets the secondary trackers' announce-list URLs.
     * 
     * @param announceList the announce-list containing list of trackers to set
     */
    public void setAnnounceList(List<String> announceList) {
        this.announceList = announceList;
    }

    /**
     * Gets the info-hash of this torrent.
     * 
     * @return the info-hash hexadecimal string
     */
    public String getInfoHash() {
        return infoHash;
    }

    /**
     * Sets the info-hash of this torrent.
     * 
     * @param infoHash the info-hash hexadecimal string to set
     */
    public void setInfoHash(String infoHash) {
        this.infoHash = infoHash;
    }
}
