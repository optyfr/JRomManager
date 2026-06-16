package jrm.digest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

/**
 * An implementation of {@link MDigest} wrapping standard JDK {@link MessageDigest} algorithms. Used for cryptographic hashing
 * algorithms such as MD5 and SHA-1.
 * 
 * @author optyfr
 */
class MsgDigest extends MDigest {
    /**
     * The underlying cryptographic {@link MessageDigest} engine.
     */
    private MessageDigest digest;

    /**
     * Constructs a new {@code MsgDigest} for the specified algorithm.
     * 
     * @param algorithm the hashing algorithm to instantiate
     * 
     * @throws NoSuchAlgorithmException if the underlying cryptographic provider does not support the algorithm
     * @throws NullPointerException if the specified {@code algorithm} parameter is {@code null}
     */
    MsgDigest(Algo algorithm) throws NoSuchAlgorithmException {
        digest = MessageDigest.getInstance(algorithm.getName());
    }

    /**
     * Updates the digest with the specified byte array input.
     * 
     * @param input the byte array input to update the digest with
     * @param offset the start offset in the byte array
     * @param len the length of bytes to read from the input, starting from the offset
     */
    @Override
    public void update(byte[] input, int offset, int len) {
        digest.update(input, offset, len);
    }

    /**
     * Completes the hash computation and returns the final hash formatted as a lower-case hexadecimal string.
     * 
     * @return the lower-case hexadecimal representation of the calculated hash
     */
    @Override
    public String toString() {
        return Hex.encodeHexString(digest.digest());
    }

    /**
     * Gets the matching {@link Algo} representation of the current message digest algorithm.
     * 
     * @return the matching {@link Algo} enum instance
     */
    @Override
    public Algo getAlgorithm() {
        return Algo.fromName(digest.getAlgorithm());
    }

    /**
     * Resets the message digest engine to its initial state for reuse.
     */
    @Override
    public void reset() {
        digest.reset();
    }
}
