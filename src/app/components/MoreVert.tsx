import React, { useState } from 'react';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import { IconButton, Menu, MenuItem } from '@mui/material';

const MoreVert = () => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleEdit = () => {
    handleClose();
    console.log('수정 클릭됨');
  };

  const handleDelete = () => {
    handleClose();
    console.log('삭제 클릭됨');
  };

  return (
    <>
      <IconButton onClick={handleClick}>
        <MoreVertIcon />
      </IconButton>
      <Menu
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        PaperProps={{
          style: {
            borderRadius: 8,
            minWidth: 120,
          },
        }}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
      >
        <MenuItem
            onClick={handleEdit}
            sx={{
                justifyContent: 'center',
                borderRadius: '10px',
                '&:hover': {
                backgroundColor: '#f0f0f0',
                fontWeight: 'bold',
                },
            }}
            >
            수정
            </MenuItem>
            <MenuItem
            onClick={handleDelete}
            sx={{
                justifyContent: 'center',
                borderRadius: '10px',
                '&:hover': {
                backgroundColor: '#f0f0f0',
                fontWeight: 'bold',
                },
            }}
            >
            삭제
        </MenuItem>
      </Menu>
    </>
  );
};

export default MoreVert;
