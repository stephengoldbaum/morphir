import json
import os
from pathlib import Path
from typing import Type, TypeVar, Optional, List
from datathread import Identifier
from metastore import Metastore

T = TypeVar('T')

class FileMetastore(Metastore):
    def __init__(self, base_dir: Path):
        self.base_dir = base_dir.resolve()

    def read(self, id: Identifier, tipe: Type[T]) -> Optional[T]:
        file_path = self.resolve_for_id(self.base_dir, id, tipe)
        return self.load_from_file(file_path, tipe)

    def read_all(self, meta_type: Type[T]) -> List[T]:
        file_suffix = self.class_name_to_file_style(meta_type)
        results = []

        for root, _, files in os.walk(self.base_dir):
            for file in files:
                if file.endswith(f"{file_suffix}.json"):
                    file_path = Path(root) / file
                    obj = self.load_from_file(file_path, meta_type)
                    if obj:
                        results.append(obj)

        return results

    def write(self, id: Identifier, data: T) -> Optional[str]:
        abs_path = self.resolve_for_id(self.base_dir, id, type(data))

        try:
            json_data = json.dumps(data, default=lambda o: o.__dict__)

            folder = abs_path.parent
            if not folder.exists():
                folder.mkdir(parents=True, exist_ok=True)

            with open(abs_path, 'w') as f:
                f.write(json_data)

            return None
        except IOError as e:
            print(e)
            return f"Failed to store id {id}"

    def delete(self, id: Identifier) -> Optional[str]:
        try:
            file_path = self.resolve_for_id(self.base_dir, id, object)
            if file_path.exists():
                file_path.unlink()
            return None
        except IOError as e:
            print(e)
            return f"Failed to delete id {id}"

    @staticmethod
    def class_name_to_file_style(tipe: Type) -> str:
        return FileMetastore.to_file_style(tipe.__name__)

    @staticmethod
    def to_file_style(s: str) -> str:
        return ''.join(['_' + c.lower() if c.isupper() else c for c in s]).lstrip('_')

    @staticmethod
    def escape(s: str) -> str:
        return s.replace("%3F", "?").replace(" ", "%20")

    @staticmethod
    def unescape(s: str) -> str:
        return s.replace("%20", " ").replace("?", "%3F")

    @staticmethod
    def resolve_for_id(base_dir: Path, id: Identifier, tipe: Type) -> Path:
        return FileMetastore.resolve_file(base_dir, id.scheme, id.domain, id.name, FileMetastore.class_name_to_file_style(tipe))

    @staticmethod
    def resolve_file(base_dir: Path, scheme: str, domain: List[str], name: str, file_suffix: str) -> Path:
        folders = [FileMetastore.unescape(d) for d in domain]
        filename = FileMetastore.unescape(name)
        path = base_dir.joinpath(*folders, f"{filename}.{file_suffix}.json").resolve()
        if not path.is_relative_to(base_dir):
            raise ValueError("Attempted directory traversal attack detected")
        return path

    def load_from_file(self, path: Path, tipe: Type[T]) -> Optional[T]:
        try:
            if path.exists():
                with open(path, 'r') as f:
                    json_data = f.read()
                    obj = json.loads(json_data, object_hook=lambda d: tipe(**d))
                    return obj
        except IOError as e:
            print(e)

        return None
