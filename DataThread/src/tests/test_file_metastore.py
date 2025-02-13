import unittest
from unittest.mock import patch, mock_open, MagicMock
from pathlib import Path
from datathread import Identifier
from datathread.metastore import FileMetastore

class TestFileMetastore(unittest.TestCase):
    def setUp(self):
        self.base_dir = Path('/fake/dir')
        self.metastore = FileMetastore(self.base_dir)

    @patch('builtins.open', new_callable=mock_open, read_data='{"name": "test"}')
    @patch('os.path.exists', return_value=True)
    def test_read(self, mock_exists, mock_open):
        id = Identifier('scheme', ['domain'], 'name')
        result = self.metastore.read(id, dict)
        self.assertEqual(result, {"name": "test"})

    @patch('os.walk')
    @patch('builtins.open', new_callable=mock_open, read_data='{"name": "test"}')
    def test_read_all(self, mock_open, mock_walk):
        mock_walk.return_value = [
            (str(self.base_dir), [], ['test.json'])
        ]
        result = self.metastore.read_all(dict)
        self.assertEqual(result, [{"name": "test"}])

    @patch('builtins.open', new_callable=mock_open)
    @patch('os.path.exists', return_value=False)
    @patch('os.makedirs')
    def test_write(self, mock_makedirs, mock_exists, mock_open):
        id = Identifier('scheme', ['domain'], 'name')
        result = self.metastore.write(id, {"name": "test"})
        self.assertIsNone(result)

    @patch('os.path.exists', return_value=True)
    @patch('os.remove')
    def test_delete(self, mock_remove, mock_exists):
        id = Identifier('scheme', ['domain'], 'name')
        result = self.metastore.delete(id)
        self.assertIsNone(result)

if __name__ == '__main__':
    unittest.main()