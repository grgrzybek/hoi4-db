/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package grgr.hoi4db.dataformat;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.base.ParserBase;
import com.fasterxml.jackson.core.io.IOContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hoi4DbParser extends ParserBase {

    private static final Logger LOG = LoggerFactory.getLogger(Hoi4DbParser.class);

    private static final char[] CHARS_NAME = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_".toCharArray();

    protected boolean docStart = true;

    private Reader reader;
    private boolean bomRead;

    // to read data from Reader
    protected char[] _inputBuffer;

    // Name, which turned out to be field name of anonymous root scope instead of the scope name itself
    private String _currentName;

    // Usually a token (field name) which may turn out to be array value instead
    private String _nextName;
    // TO store next value when detecting what the scope is
    private Object _nextValue;

    public Hoi4DbParser(IOContext ctxt, int features, Reader reader) {
        super(ctxt, features);
        this.reader = reader;
        _inputBuffer = ctxt.allocTokenBuffer();
    }

    @Override
    protected void _closeInput() throws IOException {
        reader.close();
        reader = null;
    }

    @Override
    protected void _releaseBuffers() throws IOException {
        _ioContext.releaseTokenBuffer(_inputBuffer);
    }

    @Override
    public ObjectCodec getCodec() {
        return null;
    }

    @Override
    public void setCodec(ObjectCodec c) {

    }

    @Override
    public JsonToken nextToken() throws IOException {
        int c;
        Hoi4Token ht;
        if (docStart) {
            // we're just starting. there may be:
            // - comment or whitespace
            // - name = value
            // - name = { ...
            // in case of "name = value" at the begining I'm going to wrap this value (and all following) inside anonymous root object
            // in case of "name = { ..." at the beginning, there'll be named root object
            c = skipWsAndComments();
            if (c == -1) {
                _reportInvalidEOF("Expected anonymous or named scope", null);
            }
            if (!(nameChar(c) || c == '"' || c == '-' || c == '+')) {
                _reportUnexpectedChar(c, "Expected start of literal/quoted value or number");
            }
            ht = findToken();
            if (ht == Hoi4Token.FIELD) {
                // it still may be a first field inside anonymous, implicit root scope
                c = skipWsAndComments();

                // at doc start, we always have start of object
                _currToken = JsonToken.START_OBJECT;
                if (c != '{') {
                    // we have anonymous scope and what we've found is actually first field in this scope
                    _nextToken = JsonToken.FIELD_NAME;
                    // but we have to preserver previous name which turned out to be field name inside anonymous scope
                    _nextName = _parsingContext.getCurrentName();
                    _parsingContext.setCurrentName("<anonymous>");
                } else {
                    ++_inputPtr; // to skip '{'
                }
                _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
                docStart = false;

                return _currToken;
            }
            _reportError("Expected start of scope or first field of anonymous scope");
            return null;
        } else {
            // we're in some scope and we won't leave it till EOF
            // we expect fields and values (which may be scopes or arrays)
            // scope itself (started with '{') may be an array
            // for example:
            //    allowed_module_categories = { ship_anti_air }
            //    allowed_module_categories = { ship_fire_control_system ship_sonar }
            // are both named arrays (fields inside parent scope with space-separated values), while:
            //    ai_research_weights = { offensive = -1.0 }
            // is normal new named scope with one field

            if (_nextToken != null) {
                // we were left with field name from STATE_START_DOC state, where we found anonymous root scope
                _currToken = _nextToken;
                if (!_parsingContext.inArray()) {
                    _parsingContext.setCurrentName(_nextName);
                }
                _parsingContext.setCurrentValue(_nextValue);
                _nextToken = null;
                _nextName = null;
                _nextValue = null;

                return _currToken;
            }

            switch (_currToken) {
                case START_OBJECT:
                    if (!thereIsMore()) {
                        _reportInvalidEOF("Reached end of file, expected a field", _currToken);
                    }
                    c = skipWsAndComments();
                    if (c == '}') {
                        // end of object - possibly empty, but let's allow this
                        _currToken = JsonToken.END_OBJECT;
                        _parsingContext = _parsingContext.clearAndGetParent();
                        ++_inputPtr;
                        return _currToken;
                    }
                    ht = findToken();
                    if (ht == Hoi4Token.FIELD) {
                        _currToken = JsonToken.FIELD_NAME;
                        return _currToken;
                    } else {
                        _reportError("Expected a field name inside object, found " + ht);
                    }
                    break;
                case START_ARRAY:
                    if (!thereIsMore()) {
                        _reportInvalidEOF("Reached end of file, expected an array item", _currToken);
                    }
                    c = skipWsAndComments();
                    if (c == '}') {
                        // end of array - possibly empty, but let's allow this
                        _currToken = JsonToken.END_ARRAY;
                        _parsingContext = _parsingContext.clearAndGetParent();
                        ++_inputPtr;
                        return _currToken;
                    }
                    parseValue();
                    if (_currToken == JsonToken.NOT_AVAILABLE) {
                        _reportError("Expected an array item");
                    }
                    return _currToken;
                case FIELD_NAME:
                    if (!thereIsMore()) {
                        _reportInvalidEOF("Reached end of file, expected a field", _currToken);
                    }
                    c = skipWsAndComments();
                    if (c == '{') {
                        // the value is another scope - that's fine, but we should check what this scope is
                        ++_inputPtr;
                        c = skipWsAndComments();
                        if (c == '}') {
                            // empty array
                            _currToken = JsonToken.START_ARRAY;
                            _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
                            return _currToken;
                        }
                        String currentName = _parsingContext.getCurrentName();
                        if (c == '"' || c == '-' || c == '+') {
                            // we already know it's an array item
                            ht = Hoi4Token.ITEM;
                        } else {
                            ht = findToken();
                        }
                        if (ht == Hoi4Token.FIELD) {
                            _currToken = JsonToken.START_OBJECT;
                            _nextToken = JsonToken.FIELD_NAME;
                            _nextValue = null;
                            _nextName = _parsingContext.getCurrentName();
                            _parsingContext.setCurrentName(currentName);
                            _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
                        } else if (ht == Hoi4Token.ITEM) {
                            // token turned out to be an array item, so it's a value to parse
                            _currToken = JsonToken.START_ARRAY;
                            _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
                            _nextToken = parseValue(_nextName);
                            _nextValue = _parsingContext.getCurrentValue();
                        }
                        _parsingContext.setCurrentValue(null);
                        return _currToken;
                    } else {
                        // the value should be primitive. Don't distinguish string valus and
                        // references at parser level
                        _currToken = parseValue();
                        if (_currToken == JsonToken.NOT_AVAILABLE) {
                            _reportError("Expected a value for field");
                        }
                        return _currToken;
                    }
                case END_OBJECT:
                case END_ARRAY:
                case VALUE_STRING:
                case VALUE_NUMBER_FLOAT:
                case VALUE_NUMBER_INT:
                case VALUE_TRUE:
                case VALUE_FALSE:
                    JsonToken t = progressToNextToken();
                    if (t == null) {
                        // we are always in some scope
                        _parsingContext = _parsingContext.clearAndGetParent();
                        if (_parsingContext == null) {
                            return null;
                        }
                        if (_parsingContext.getParent() != null) {
                            _reportInvalidEOF("Reached end of file, but not all scopes were closed", _currToken);
                        }
                        _currToken = JsonToken.END_OBJECT;
                        return _currToken;
                    }

                    // after end of scope/array, and after a value we expect end of parent scope or next field (not item)
                    c = skipWsAndComments();
                    _parsingContext.setCurrentValue(null);
                    if (c == '}') {
                        if (_parsingContext.inObject()) {
                            _currToken = JsonToken.END_OBJECT;
                        } else {
                            _currToken = JsonToken.END_ARRAY;
                        }
                        _parsingContext = _parsingContext.clearAndGetParent();
                        ++_inputPtr;
                    } else {
                        if (_parsingContext.inArray()) {
                            ht = Hoi4Token.ITEM;
                        } else {
                            ht = findToken();
                        }
                        if (ht == Hoi4Token.ITEM) {
                            _currToken = parseValue();
                        } else if (ht == Hoi4Token.FIELD) {
                            _currToken = JsonToken.FIELD_NAME;
                        } else {
                            _reportError("Expected next field or end of scope, found " + ht);
                        }
                    }
                    return _currToken;
                default:
            }

            _reportError("Expected field name or array item");
            return null;
        }
    }

    /**
     * Ensures that {@link #_inputBuffer} has something to read and that {@link #_inputPtr} is smaller than {@link #_inputEnd}.
     * If there's nothing more, {@link #_inputPtr} will be equal to {@link #_inputEnd} and we'll return {@code false}.
     *
     * @return
     */
    private boolean thereIsMore() {
        if (_inputPtr < _inputEnd) {
            return true;
        }
        if (isClosed()) {
            return false;
        }
        // can we read more?
        try {
            int count = reader.read(_inputBuffer, 0, _inputBuffer.length);
            if (count > 0) {
                _inputPtr = 0;
                _inputEnd = count;
                if (!bomRead) {
                    if (_inputBuffer[0] == 0xEF) {
                        // could be UTF-8 BOM
                        if (count < 3) {
                            throw new IOException("Found illegal Byte Order Mark");
                        }
                        if (!(_inputBuffer[1] == 0xBB && _inputBuffer[2] == 0xBF)) {
                            throw new IOException("Found illegal Byte Order Mark, expected UTF-8 one");
                        }
                        _inputPtr += 3;
                    } else if (_inputBuffer[0] == '\uFEFF') {
                        _inputPtr++;
                    }
                    bomRead = true;
                }
                return true;
            }
            close();

            if (count == 0) {
                throw new IOException("Reader returned 0 characters");
            }

            return false;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Stops at next char that's not a comment, new line or space. It may be EOF. It returns this non WS char.
     *
     * @return
     * @throws IOException
     */
    private int skipWsAndComments() throws IOException {
        updateLocation();
        boolean inComment = false;
        boolean hadCr = false;
        while (thereIsMore()) {
            while (_inputPtr < _inputEnd) {
                int c = _inputBuffer[_inputPtr++];
                if (hadCr && c != '\n') {
                    // Mac
                    ++_tokenInputRow;
                    ++_currInputRow;
                    _tokenInputCol = 0;
                    _currInputRowStart = _inputPtr;
                }
                if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                    if (c == '\r' || c == '\n') {
                        inComment = false;
                    }
                    if (c == '\n') {
                        ++_tokenInputRow;
                        ++_currInputRow;
                        _tokenInputCol = 0;
                        _currInputRowStart = _inputPtr;
                    }
                    hadCr = c == '\r';
                } else if (c == '#' && !inComment) {
                    inComment = true;
                } else if (!inComment) {
                    --_inputPtr;
                    return c;
                }
            }
        }

        return -1;
    }

    /**
     * Find a {@link Hoi4Token}. We'll check if it's field name or array item. This method is not for parsing values
     * of fields. However it may collect a name that will turn out to be array item (thus - a value to parse later).
     *
     * @return if name is followed by {@code =}, it's a field name (or root scope name), otherwise it's array item
     */
    private Hoi4Token findToken() throws IOException {
        updateLocation();
        boolean crossBufferNeeded = false;
        boolean gotName = false;

        char[] outBuf = null;
        int outPtr = -1;

        // iterate for token - even across buffers
        while (thereIsMore()) {
            int start = _inputPtr;

            // iterate within single buffer
            while (_inputPtr < _inputEnd) {
                char c = _inputBuffer[_inputPtr];
                if (!nameChar(c)) {
                    gotName = true;
                    break;
                }
                if (outBuf != null) {
                    // we're collecting token in segmented buffer
                    outBuf[outPtr++] = c;
                    // which again may be too small
                    if (outPtr >= outBuf.length) {
                        outBuf = _textBuffer.finishCurrentSegment();
                        outPtr = 0;
                    }
                }
                ++_inputPtr;
            }
            // after checking single buffer we may, or may not find a char marking end of token
            if (gotName) {
                if (outBuf == null) {
                    // simple case - extract token from current buffer
                    _nextName = new String(_inputBuffer, start, _inputPtr - start);
                }
                break;
            } else if (outBuf == null) {
                // name will (possibly) be split across buffers
                _textBuffer.resetWithShared(_inputBuffer, start, _inputPtr - start);
                outBuf = _textBuffer.getCurrentSegment();
                outPtr = _textBuffer.getCurrentSegmentSize();
            }
        }

        if (outBuf != null) {
            // extract token from segmented buffer
            _textBuffer.setCurrentLength(outPtr);
            _nextName = new String(_textBuffer.getTextBuffer(), _textBuffer.getTextOffset(), _textBuffer.size());
        }

        // check what's the kind of token:
        // - field name must be followed by '='
        // - array element must be followed by whitespace, next name or '}' - in case of array, token is actually
        //   a value and its name should be that of parent scope

        int c = skipWsAndComments();
        if (c == '=') {
            // we can safely say that this token is a name
            _parsingContext.setCurrentName(_nextName);
            _nextName = null;
            ++_inputPtr;
            skipWsAndComments();
            return Hoi4Token.FIELD;
        }
        if (c == '}' || nameChar(c)) {
            // _nextName turns out to be a value - to be parsed later
            return Hoi4Token.ITEM;
        }

        _reportUnexpectedChar(c, "Invalid character after field name");
        return Hoi4Token.UNKNOWN;
    }

    /**
     * This method progresses through buffer and tries to find a value. It updates the pointers.
     */
    private JsonToken parseValue() throws JsonParseException {
        updateLocation();
        boolean crossBufferNeeded = false;

        char[] outBuf = null;
        int outPtr = -1;
        boolean escape = false;
        boolean inString = false;
        boolean gotValue = false;

        String raw = null;

        // iterate for value - even across buffers
        while (thereIsMore()) {
            int start = _inputPtr;

            // iterate within single buffer
            while (_inputPtr < _inputEnd) {
                char c = _inputBuffer[_inputPtr++];
                if (c == '\\') {
                    escape = true;
                } else {
                    if (c == '"' && !escape) {
                        inString = !inString;
                        continue;
                    }
                    escape = false;
                }
                if (!inString && !nameChar(c) && !numberChar(c)) {
                    // end of value. let's allow newlines inside string
                    gotValue = true;
                    --_inputPtr;
                    break;
                }
                if (outBuf != null) {
                    // we're collecting token in segmented buffer
                    outBuf[outPtr++] = c;
                    // which may be too small
                    if (outPtr >= outBuf.length) {
                        outBuf = _textBuffer.finishCurrentSegment();
                        outPtr = 0;
                    }
                }
            }
            // after checking single buffer we may, or may not find a char marking end of value
            if (gotValue) {
                if (outBuf == null) {
                    // simple case - extract token from current buffer
                    raw = new String(_inputBuffer, start, _inputPtr - start);
                }
                break;
            } else if (outBuf == null) {
                // name will (possibly) be split across buffers
                _textBuffer.resetWithShared(_inputBuffer, start, _inputPtr - start);
                outBuf = _textBuffer.getCurrentSegment();
                outPtr = _textBuffer.getCurrentSegmentSize();
            }
        }

        if (outBuf != null) {
            // extract token from segmented buffer
            _textBuffer.setCurrentLength(outPtr);
            raw = new String(_textBuffer.getTextBuffer(), _textBuffer.getTextOffset(), _textBuffer.size());
        }

        return parseValue(raw);
    }

    /**
     * This method doesn't progress through buffer and works only on passed raw (string) value. It sets
     * value in current parsing context;
     * @param raw
     * @return
     */
    private JsonToken parseValue(String raw) throws JsonParseException {
        // check type of value: string, boolean, bigdecimal, biginteger
        if (raw == null) {
            _reportError("Can't parse value");
            return JsonToken.NOT_AVAILABLE;
        }
        raw = raw.trim();

        if (raw.length() == 0) {
            _parsingContext.setCurrentValue("");
            return JsonToken.VALUE_STRING;
        }

        char c = raw.charAt(0);
        if ((c >= '0' && c <= '9') || c == '-' || c == '+') {
            // a number
            if (raw.contains(".")) {
                _parsingContext.setCurrentValue(new BigDecimal(raw));
                return JsonToken.VALUE_NUMBER_FLOAT;
            } else {
                _parsingContext.setCurrentValue(new BigInteger(raw));
                return JsonToken.VALUE_NUMBER_INT;
            }
        }

        if ("false".equalsIgnoreCase(raw) || "no".equalsIgnoreCase(raw)) {
            _parsingContext.setCurrentValue(Boolean.FALSE);
            return JsonToken.VALUE_FALSE;
        }
        if ("true".equalsIgnoreCase(raw) || "yes".equalsIgnoreCase(raw)) {
            _parsingContext.setCurrentValue(Boolean.TRUE);
            return JsonToken.VALUE_TRUE;
        }

        if (raw.startsWith("\"") && raw.endsWith("\"")) {
            raw = raw.substring(1, raw.length() - 1);
        }
        _parsingContext.setCurrentValue(raw);
        return JsonToken.VALUE_STRING;
    }

    /**
     * After a value or end of scope we expect end of parent scope, a field or (rarely) EOF. We don't kno
     * @return
     */
    private JsonToken progressToNextToken() throws IOException {
        updateLocation();
        int c = skipWsAndComments();
        if (c == -1) {
            return null;
        }
        if (!thereIsMore()) {
            if (_parsingContext.getParent() != null) {
                _reportInvalidEOF("Reached end of file, but not all scopes were closed", _currToken);
            } else {
                // fine - let's finish parsing
                return null;
            }
        }

        // we'll take care of next token later
        return JsonToken.NOT_AVAILABLE;
    }

    private boolean nameChar(int c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '.') {
            return true;
        }
        return false;
    }

    private boolean numberChar(int c) {
        if ((c >= '0' && c <= '9') || c == '+' || c == '-' || c == '.') {
            return true;
        }
        return false;
    }

    private boolean in(char c, char ... chars) {
        for (char x : chars) {
            if (c == x) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getText() throws IOException {
        return null;
    }

    @Override
    public char[] getTextCharacters() throws IOException {
        return new char[0];
    }

    @Override
    public int getTextLength() throws IOException {
        return 0;
    }

    @Override
    public int getTextOffset() throws IOException {
        return 0;
    }

    private void updateLocation()
    {
        int ptr = _inputPtr;
        _tokenInputTotal = _currInputProcessed + ptr;
        _tokenInputRow = _currInputRow;
        _tokenInputCol = ptr - _currInputRowStart;
    }

    private enum Hoi4Token {
        /** A field name: There's always {@code =} after the name */
        FIELD,
        /** An array item: There's either {@code ,} after the name or right curly bracket meaning end of array */
        ITEM,
        /** A field value: There's either other name after the name or right curly bracket meaning end of current scope */
        VALUE,
        /** No idea... */
        UNKNOWN
    }

}
