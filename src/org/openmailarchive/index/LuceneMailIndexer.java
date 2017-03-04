package org.openmailarchive.index;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

/**
 * This file is part of OpenMailArchive.
 *
 * OpenMailArchive is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenMailArchive is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenMailArchive.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by pov on 4/03/17.
 */
public class LuceneMailIndexer {
    private IndexWriter w;

    public LuceneMailIndexer(String indexDir) {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index;
        try {
            index = new NIOFSDirectory(Paths.get(indexDir));
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            w = new IndexWriter(index, config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void indexMail(String mailId, String body, Map<String, String> attachments) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("mailId", mailId, Field.Store.YES));
        doc.add(new TextField("body", body, Field.Store.YES));
        w.addDocument(doc);

        for (String fileName:
             attachments.keySet()) {
            doc = new Document();
            doc.add(new StringField("mailId", mailId, Field.Store.YES));
            doc.add(new TextField("attachementName", fileName, Field.Store.YES));
            doc.add(new TextField("attachementBody", attachments.get(fileName), Field.Store.YES));
            w.addDocument(doc);
        }

        w.commit();
    }

    public void end() {
        try {
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
